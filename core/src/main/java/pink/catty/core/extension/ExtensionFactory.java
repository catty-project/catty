package pink.catty.core.extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.extension.spi.Serialization;

/**
 * Catty has some build-in extension interface for customizing, such as: {@link Serialization}
 * {@link pink.catty.core.Invoker} {@link InvokerChainBuilder} {@link Codec} {@link LoadBalance}.
 * And there are also some build-in implements of those extension interface you can find them in
 * extension-module. You can use Reference and Exporter(you can find both in config-module) to
 * config different implements to make Catty work in another way.
 *
 * If you want to use you own implements, you can use {@link this#register(String, Object)} method
 * to add you own and specify your extension name. There is an example of extension usage in
 * example-module.
 *
 * {@link Extension} annotation is for inner using to config extension's name, so your own extension
 * implements has no need to use this annotation.
 */
public final class ExtensionFactory<T> {

  private static final String EXTENSION_PATH = "pink.catty.extension";

  private static List<ExtensionFactory> extensionFactories;

  private static ExtensionFactory<Serialization> SERIALIZATION;
  private static ExtensionFactory<LoadBalance> LOAD_BALANCE;
  private static ExtensionFactory<Codec> CODEC;
  private static ExtensionFactory<InvokerChainBuilder> INVOKER_BUILDER;

  static {
    SERIALIZATION = new ExtensionFactory<>(Serialization.class);
    LOAD_BALANCE = new ExtensionFactory<>(LoadBalance.class);
    CODEC = new ExtensionFactory<>(Codec.class);
    INVOKER_BUILDER = new ExtensionFactory<>(InvokerChainBuilder.class);

    extensionFactories = new ArrayList<ExtensionFactory>() {
      {
        add(SERIALIZATION);
        add(LOAD_BALANCE);
        add(CODEC);
        add(INVOKER_BUILDER);
      }
    };

    try {
      loadExtension();
    } catch (ClassNotFoundException | IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static void loadExtension() throws ClassNotFoundException, IOException {
    Class<?>[] classes = getClasses(EXTENSION_PATH);
    for (Class<?> clz : classes) {
      if (!clz.isAnnotationPresent(Extension.class)) {
        continue;
      }
      Extension extension = clz.getAnnotation(Extension.class);
      for (ExtensionFactory extensionFactory : extensionFactories) {
        if (extensionFactory.getSupportedExtension().isAssignableFrom(clz)) {
          extensionFactory.register(extension.value(), clz);
        }
      }
    }
  }

  private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assert classLoader != null;
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    ArrayList<Class> classes = new ArrayList<>();
    for (File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }
    return classes.toArray(new Class[0]);
  }

  private static List<Class> findClasses(File directory, String packageName)
      throws ClassNotFoundException {
    List<Class> classes = new ArrayList<>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    if (files == null) {
      return classes;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        assert !file.getName().contains(".");
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class
            .forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }


  public static ExtensionFactory<Serialization> getSerialization() {
    return SERIALIZATION;
  }

  public static ExtensionFactory<LoadBalance> getLoadBalance() {
    return LOAD_BALANCE;
  }

  public static ExtensionFactory<Codec> getCodec() {
    return CODEC;
  }

  public static ExtensionFactory<InvokerChainBuilder> getInvokerBuilder() {
    return INVOKER_BUILDER;
  }

  /* static over */

  private Class<T> supportedExtension;
  private Map<String, T> extensionMap;
  private Map<String, Class<? extends T>> extensionClassMap;

  public ExtensionFactory(Class<T> supportedExtension) {
    this.supportedExtension = supportedExtension;
    this.extensionMap = new HashMap<>();
    this.extensionClassMap = new HashMap<>();
  }

  public Class<T> getSupportedExtension() {
    return supportedExtension;
  }

  @SuppressWarnings("unchecked")
  public void register(String name, T extension) {
    checkName(name);
    if (extensionMap.containsKey(name)) {
      throw new DuplicatedExtensionException();
    }
    extensionMap.put(name, extension);
    extensionClassMap.putIfAbsent(name, (Class<? extends T>) extension.getClass());
  }

  public void register(Enum<?> name, T extension) {
    checkName(name);
    register(name.toString(), extension);
  }

  public void register(String name, Class<? extends T> extensionClass) {
    checkName(name);
    if (extensionClassMap.containsKey(name)) {
      throw new DuplicatedExtensionException();
    }
    extensionClassMap.put(name, extensionClass);
  }

  public void register(Enum<?> name, Class<? extends T> extensionClass) {
    checkName(name);
    register(name.toString(), extensionClass);
  }

  public T getExtensionProtoType(String name, Object... args) {
    checkName(name);
    Class<? extends T> extensionClass = extensionClassMap.get(name);
    if (extensionClass == null) {
      throw new ExtensionNotFoundException(
          "Extension type: " + supportedExtension.getName() + " name: " + name);
    }
    Class[] argTypes;
    if (args == null || args.length == 0) {
      argTypes = null;
    } else {
      argTypes = new Class[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = args[i].getClass();
      }
    }
    try {
      Constructor<? extends T> constructor = extensionClass.getConstructor(argTypes);
      return constructor.newInstance(args);
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ExtensionNotFoundException();
    }
  }

  public T getExtensionProtoType(Enum<?> name, Object... args) {
    return getExtensionProtoType(name.toString(), args);
  }

  public T getExtensionSingleton(String name) {
    checkName(name);
    T extension = extensionMap.get(name);
    if (extension == null) {
      Class<? extends T> extensionClass = extensionClassMap.get(name);
      if (extensionClass == null) {
        throw new ExtensionNotFoundException(
            "Extension type: " + supportedExtension.getName() + " name: " + name);
      } else {
        extension = getExtensionProtoType(name);
        register(name, extension);
      }
    }
    return extension;
  }

  public T getExtensionSingleton(Enum<?> name) {
    return getExtensionSingleton(name.toString());
  }

  private void checkName(Object name) {
    if (name == null) {
      throw new NullPointerException("null name");
    }
  }

}
