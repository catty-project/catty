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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.EndpointFactory;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.extension.spi.Protocol;
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.extension.spi.SPI;
import pink.catty.core.extension.spi.Scope;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.utils.ReflectUtils;

/**
 * Catty has some build-in extension interface to customizing, such as: {@link Serialization} {@link
 * Invoker} {@link Protocol} {@link Codec} {@link LoadBalance} {@link EndpointFactory} {@link
 * Registry}. And there are also some build-in implements of those extension interface you can find
 * them in extension-module. You can use Reference and Exporter(you can find both in config-module)
 * to specify different implements to make Catty work in different way.
 * <p>
 * A class that implements the extension interface is expected to have a constructor with no
 * parameter.
 * <p>
 * Every extension implements in extension-module will be auto registered in ExtensionFactory when
 * ExtensionFactory class initializing.
 * <p>
 * If you want to use you own implements, you can use {@link this#register(String, Object)} method
 * to add you own and specify your extension name. There is an example of extension usage in
 * example-module.
 * <p>
 * {@link Extension} annotation is for inner using to config extension's name, so your own extension
 * implements has no need to use this annotation.
 *
 * @see Serialization
 * @see Codec
 * @see LoadBalance
 * @see EndpointFactory
 * @see Registry
 * @see Invoker
 * @since 0.2.7 SPI is supported.
 */
public final class ExtensionFactory<T> {

  private static final Logger logger = LoggerFactory.getLogger(ExtensionFactory.class);

  private static final String EXTENSION_PATH = "pink.catty.extension";
  private static final String CLASS_SUFFIX = ".class";
  private static final int CLASS_SUFFIX_LENGTH = CLASS_SUFFIX.length();
  private static final String JAR_PROTOCOL = "jar";
  private static final String FILE_PROTOCOL = "file";
  private static final String DEFAULT_CHARACTER = "utf-8";
  private static final String PREFIX = "META-INF/services/";

  private static final Map<Class<?>, ExtensionFactory<?>> EXTENSION_FACTORY = new ConcurrentHashMap<>();

  static {
    EXTENSION_FACTORY.put(Serialization.class, new ExtensionFactory<>(Serialization.class));
    EXTENSION_FACTORY.put(LoadBalance.class, new ExtensionFactory<>(LoadBalance.class));
    EXTENSION_FACTORY.put(Codec.class, new ExtensionFactory<>(Codec.class));
    EXTENSION_FACTORY.put(Protocol.class, new ExtensionFactory<>(Protocol.class));
    EXTENSION_FACTORY.put(EndpointFactory.class, new ExtensionFactory<>(EndpointFactory.class));
    EXTENSION_FACTORY.put(Registry.class, new ExtensionFactory<>(Registry.class));

    try {

      /*
       * First: Scan pink.catty.extension package and sub-package, register all extensions' instance to ExtensionFactory.
       *
       * Second: Pre-scan SPI file under META-INF/services/. SPI file should be composited of key-value pairs of each line.
       *         In this stage, Catty would find all extension implement class and register name-class pair to ExtensionFactory.
       *
       */

      logger.debug("Extension: begin loading extension...");
      loadExtension();
      preLoadSpi();
      logger.debug("Extension: loading extension finished...");
    } catch (ClassNotFoundException | IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static void loadExtension() throws ClassNotFoundException, IOException {
    Class<?>[] classes = getClasses(EXTENSION_PATH);
    for (Class<?> clz : classes) {
      if (!clz.isAnnotationPresent(Extension.class)) {
        logger.debug(
            "Extension: Extension.class annotation not present at {}, this class would not be loaded.",
            clz.toString());
        continue;
      }
      Extension extension = clz.getAnnotation(Extension.class);
      for (ExtensionFactory extensionFactory : EXTENSION_FACTORY.values()) {
        if (extensionFactory.getSupportedExtension().isAssignableFrom(clz)) {
          extensionFactory.register(extension.value(), clz);
          logger
              .debug("Extension: register an extension: {}, {}", extension.value(), clz.toString());
        }
      }
    }
  }

  private static Class<?>[] getClasses(String packageName)
      throws ClassNotFoundException, IOException {
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
    ArrayList<Class<?>> classes = new ArrayList<>();
    for (File directory : dirs) {
      classes.addAll(findClassesByDir(directory, packageName));
    }
    for (JarFile jar : jars) {
      classes.addAll(findClassesByJar(jar, packageName));
    }
    return classes.toArray(new Class[0]);
  }

  private static List<Class<?>> findClassesByDir(File directory, String packageName)
      throws ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<>();
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

  private static List<Class<?>> findClassesByJar(JarFile jar, String packageName)
      throws ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<>();
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

  @SuppressWarnings("unchecked")
  private static void preLoadSpi() throws IOException, ClassNotFoundException {
    for (ExtensionFactory factory : EXTENSION_FACTORY.values()) {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String fullName = PREFIX + factory.supportedExtension.getName();
      Enumeration<URL> urls;
      if (classLoader == null) {
        urls = ClassLoader.getSystemResources(fullName);
      } else {
        urls = classLoader.getResources(fullName);
      }

      // spi file not found.
      if (urls == null || !urls.hasMoreElements()) {
        continue;
      }

      while (urls.hasMoreElements()) {
        URL url = urls.nextElement();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(url.openStream(), DEFAULT_CHARACTER));
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.length() == 0) {
            continue;
          }

          int ci = line.indexOf('#');
          if (ci == 0) {
            continue;
          }

          String[] split = line.split("=");
          if (split.length != 2) {
            throw new SpiSyntaxException("Each line must like 'name=com.a.b.ClassName'");
          }

          String name = split[0];
          Class<?> implementClass = Class.forName(split[1]);
          factory.register(name, implementClass);
        }
      }
    }
  }

  /* public static method */

  /*
   * Convenient functions to get inner extension factory.
   */

  /**
   * It is a shortcut for ExtensionFactory.getExtensionFactory(Protocol.class)
   *
   * @return ExtensionFactory<Protocol>
   */
  public static ExtensionFactory<Protocol> getProtocol() {
    return getExtensionFactory(Protocol.class);
  }

  /**
   * It is a shortcut for ExtensionFactory.getExtensionFactory(Codec.class)
   *
   * @return ExtensionFactory<Codec>
   */
  public static ExtensionFactory<Codec> getCodec() {
    return getExtensionFactory(Codec.class);
  }

  /**
   * It is a shortcut for ExtensionFactory.getExtensionFactory(Serialization.class)
   *
   * @return ExtensionFactory<Serialization>
   */
  public static ExtensionFactory<Serialization> getSerialization() {
    return getExtensionFactory(Serialization.class);
  }

  /**
   * It is a shortcut for ExtensionFactory.getExtensionFactory(EndpointFactory.class)
   *
   * @return ExtensionFactory<EndpointFactory>
   */
  public static ExtensionFactory<EndpointFactory> getEndpointFactory() {
    return getExtensionFactory(EndpointFactory.class);
  }

  /**
   * It is a shortcut for ExtensionFactory.getExtensionFactory(LoadBalance.class)
   *
   * @return ExtensionFactory<LoadBalance>
   */
  public static ExtensionFactory<LoadBalance> getLoadBalance() {
    return getExtensionFactory(LoadBalance.class);
  }

  /**
   * It is a shortcut for ExtensionFactory.getExtensionFactory(Registry.class)
   *
   * @return ExtensionFactory<Registry>
   */
  public static ExtensionFactory<Registry> getRegistry() {
    return getExtensionFactory(Registry.class);
  }

  @SuppressWarnings("unchecked")
  public static <E> ExtensionFactory<E> getExtensionFactory(Class<E> extensionClass) {
    return (ExtensionFactory<E>) EXTENSION_FACTORY.get(extensionClass);
  }

  /* static over */

  private final Class<T> supportedExtension;
  private final Map<String, T> extensionMap;
  private final Map<String, Class<? extends T>> extensionClassMap;
  private final Scope scope;

  public ExtensionFactory(Class<T> supportedExtension) {
    this.supportedExtension = supportedExtension;
    this.extensionMap = new HashMap<>();
    this.extensionClassMap = new HashMap<>();
    SPI spi = supportedExtension.getAnnotation(SPI.class);
    if (spi == null) {
      throw new RuntimeException("Class has no SPI.class annotation, class: " + supportedExtension.getName());
    }
    scope = spi.scope();
  }

  public Class<T> getSupportedExtension() {
    return supportedExtension;
  }

  @SuppressWarnings("unchecked")
  public void register(String name, T extension) {
    Objects.requireNonNull(name, "null name");
    if (extensionMap.containsKey(name)) {
      throw new DuplicatedExtensionException();
    }
    extensionMap.put(name, extension);
    extensionClassMap.putIfAbsent(name, (Class<? extends T>) extension.getClass());
  }

  public void register(String name, Class<? extends T> extensionClass) {
    Objects.requireNonNull(name, "null name");
    if (extensionClassMap.containsKey(name)) {
      throw new DuplicatedExtensionException();
    }
    extensionClassMap.put(name, extensionClass);
  }

  public T getExtension(String name) {
    if (scope == Scope.PROTOTYPE) {
      return getExtensionPrototype(name);
    } else if (scope == Scope.SINGLETON) {
      return getExtensionSingleton(name);
    } else {
      return getExtensionPrototype(name);
    }
  }

  /**
   * Get an singleton instance of an extension.
   *
   * @param name extension name.
   * @param args extension's constructor's arguments.
   * @return an instance of extension.
   */
  private T getExtensionSingleton(String name, Object... args) {
    Objects.requireNonNull(name, "null name");
    T extension = extensionMap.get(name);
    if (extension == null) {
      synchronized (supportedExtension) {
        extension = extensionMap.get(name);
        if (extension == null) {
          Class<? extends T> extensionClass = extensionClassMap.get(name);
          if (extensionClass == null) {
            throw new ExtensionNotFoundException(
                "Extension type: " + supportedExtension.getName() + " name: " + name);
          } else {
            extension = getExtensionPrototype(name, args);
            register(name, extension);
          }
        }
      }
    }
    return extension;
  }

  /**
   * Generate an extension prototype instance.
   *
   * @param name extension name.
   * @param args extension's constructor's arguments.
   * @return an instance of extension.
   */
  private T getExtensionPrototype(String name, Object... args) {
    Objects.requireNonNull(name, "null name");
    Class<? extends T> extensionClass = extensionClassMap.get(name);
    if (extensionClass == null) {
      throw new ExtensionNotFoundException(
          "Extension type: " + supportedExtension.getName() + " name: " + name);
    }
    try {
      Constructor<? extends T> constructor = ReflectUtils.resolveConstructor(extensionClass, args);
      return constructor.newInstance(args);
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ExtensionNotFoundException();
    }
  }
}
