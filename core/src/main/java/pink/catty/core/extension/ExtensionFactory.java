/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.core.extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.EndpointFactory;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.invoker.Invoker;

/**
 * Catty has some build-in extension interface for customizing, such as: {@link Serialization}
 * {@link Invoker} {@link InvokerChainBuilder} {@link Codec} {@link LoadBalance} {@link
 * EndpointFactory} {@link Registry}. And there are also some build-in implements of those extension
 * interface you can find them in extension-module. You can use Reference and Exporter(you can find
 * both in config-module) to config different implements to make Catty work in another way.
 *
 * Every extension implements in extension-module will be auto registered in ExtensionFactory when
 * ExtensionFactory class initializing.
 *
 * If you want to use you own implements, you can use {@link this#register(String, Object)} method
 * to add you own and specify your extension name. There is an example of extension usage in
 * example-module.
 *
 * {@link Extension} annotation is for inner using to config extension's name, so your own extension
 * implements has no need to use this annotation.
 */
public final class ExtensionFactory<T> {

  private static final Logger logger = LoggerFactory.getLogger(ExtensionFactory.class);

  private static final String EXTENSION_PATH = "pink.catty.extension";
  private static final String CLASS_SUFFIX = ".class";
  private static final int CLASS_SUFFIX_LENGTH = CLASS_SUFFIX.length();
  private static final String JAR_PROTOCOL = "jar";
  private static final String FILE_PROTOCOL = "file";

  private static ExtensionFactory<Serialization> SERIALIZATION;
  private static ExtensionFactory<LoadBalance> LOAD_BALANCE;
  private static ExtensionFactory<Codec> CODEC;
  private static ExtensionFactory<InvokerChainBuilder> INVOKER_BUILDER;
  private static ExtensionFactory<EndpointFactory> ENDPOINT_FACTORY;
  private static ExtensionFactory<Registry> REGISTRY;

  static {
    SERIALIZATION = new ExtensionFactory<>(Serialization.class);
    LOAD_BALANCE = new ExtensionFactory<>(LoadBalance.class);
    CODEC = new ExtensionFactory<>(Codec.class);
    INVOKER_BUILDER = new ExtensionFactory<>(InvokerChainBuilder.class);
    ENDPOINT_FACTORY = new ExtensionFactory<>(EndpointFactory.class);
    REGISTRY = new ExtensionFactory<>(Registry.class);

    try {
      logger.debug("Extension: begin loading extension...");
      loadExtension();
      logger.debug("Extension: loading extension finished...");
    } catch (ClassNotFoundException | IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static void loadExtension() throws ClassNotFoundException, IOException {
    List<ExtensionFactory> extensionFactories = new ArrayList<ExtensionFactory>() {
      {
        add(SERIALIZATION);
        add(LOAD_BALANCE);
        add(CODEC);
        add(INVOKER_BUILDER);
        add(ENDPOINT_FACTORY);
        add(REGISTRY);
      }
    };
    Class<?>[] classes = getClasses(EXTENSION_PATH);
    for (Class<?> clz : classes) {
      if (!clz.isAnnotationPresent(Extension.class)) {
        logger.debug(
            "Extension: Extension.class annotation not present at {}, this class would not be loaded.",
            clz.toString());
        continue;
      }
      Extension extension = clz.getAnnotation(Extension.class);
      for (ExtensionFactory extensionFactory : extensionFactories) {
        if (extensionFactory.getSupportedExtension().isAssignableFrom(clz)) {
          extensionFactory.register(extension.value(), clz);
          logger
              .debug("Extension: register an extension: {}, {}", extension.value(), clz.toString());
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
    List<JarFile> jars = new ArrayList<>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      if (JAR_PROTOCOL.equals(resource.getProtocol())) {
        JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
        jars.add(jarFile);
      }
      if (FILE_PROTOCOL.equals(resource.getProtocol())) {
        dirs.add(new File(resource.getFile()));
      }
    }
    ArrayList<Class> classes = new ArrayList<>();
    for (File directory : dirs) {
      classes.addAll(findClassesByDir(directory, packageName));
    }
    for (JarFile jar : jars) {
      classes.addAll(findClassesByJar(jar, packageName));
    }
    return classes.toArray(new Class[0]);
  }

  private static List<Class> findClassesByDir(File directory, String packageName)
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
        classes.addAll(findClassesByDir(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class
            .forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }

  private static List<Class> findClassesByJar(JarFile jar, String packageName)
      throws ClassNotFoundException {
    List<Class> classes = new ArrayList<>();
    Enumeration<JarEntry> entries = jar.entries();
    String packageDir = packageName.replace(".", File.separator);
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if (entry.isDirectory() || !entry.getName().startsWith(packageDir)
          || !entry.getName().endsWith(CLASS_SUFFIX)) {
        continue;
      }
      String entryName = entry.getName();
      int entryNameLength = entry.getName().length();
      String classPath = entryName.substring(0, entryNameLength - CLASS_SUFFIX_LENGTH);
      Class<?> clazz = Class.forName(classPath.replace(File.separator, "."));
      classes.add(clazz);
    }
    return classes;
  }


  /* public static method */

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

  public static ExtensionFactory<EndpointFactory> getEndpointFactory() {
    return ENDPOINT_FACTORY;
  }

  public static ExtensionFactory<Registry> getRegistry() {
    return REGISTRY;
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

  public void register(String name, Class<? extends T> extensionClass) {
    checkName(name);
    if (extensionClassMap.containsKey(name)) {
      throw new DuplicatedExtensionException();
    }
    extensionClassMap.put(name, extensionClass);
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

  public T getExtensionSingleton(String name, Object... args) {
    checkName(name);
    T extension = extensionMap.get(name);
    if (extension == null) {
      Class<? extends T> extensionClass = extensionClassMap.get(name);
      if (extensionClass == null) {
        throw new ExtensionNotFoundException(
            "Extension type: " + supportedExtension.getName() + " name: " + name);
      } else {
        extension = getExtensionProtoType(name, args);
        register(name, extension);
      }
    }
    return extension;
  }

  private void checkName(Object name) {
    if (name == null) {
      throw new NullPointerException("null name");
    }
  }

}
