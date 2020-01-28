package io.catty.extension;

import io.catty.builder.CattyInvokerBuilder;
import io.catty.codec.CattyCodec;
import io.catty.codec.CattySerialization;
import io.catty.codec.Codec;
import io.catty.codec.Serialization;
import io.catty.core.InvokerChainBuilder;
import io.catty.extension.ExtensionType.CodecType;
import io.catty.extension.ExtensionType.InvokerBuilderType;
import io.catty.extension.ExtensionType.LoadBalanceType;
import io.catty.extension.ExtensionType.SerializationType;
import io.catty.lbs.LoadBalance;
import io.catty.lbs.RandomLoadBalance;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class ExtensionFactory<T> {

  private static ExtensionFactory<Serialization> SERIALIZATION;
  private static ExtensionFactory<LoadBalance> LOAD_BALANCE;
  private static ExtensionFactory<Codec> CODEC;
  private static ExtensionFactory<InvokerChainBuilder> INVOKER_BUILDER;

  static {
    SERIALIZATION = new ExtensionFactory<>();
    LOAD_BALANCE = new ExtensionFactory<>();
    CODEC = new ExtensionFactory<>();
    INVOKER_BUILDER = new ExtensionFactory<>();

    SERIALIZATION.register(SerializationType.PROTOBUF_FASTJSON, new CattySerialization());
    LOAD_BALANCE.register(LoadBalanceType.RANDOM, RandomLoadBalance.class);
    CODEC.register(CodecType.CATTY, new CattyCodec());
    INVOKER_BUILDER.register(InvokerBuilderType.DIRECT, new CattyInvokerBuilder());
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

  private Map<String, T> extensionMap;
  private Map<String, Class<? extends T>> extensionClassMap;

  public ExtensionFactory() {
    extensionMap = new HashMap<>();
    extensionClassMap = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  public void register(String name, T extension) {
    if (extensionMap.containsKey(name)) {
      throw new DuplicatedExtensionException();
    }
    extensionMap.put(name, extension);
    extensionClassMap.putIfAbsent(name, (Class<? extends T>) extension.getClass());
  }

  private void register(Enum<?> name, T extension) {
    register(name.toString(), extension);
  }

  public void register(String name, Class<? extends T> extensionClass) {
    if (extensionClassMap.containsKey(name)) {
      throw new DuplicatedExtensionException();
    }
    extensionClassMap.put(name, extensionClass);
  }

  private void register(Enum<?> name, Class<? extends T> extensionClass) {
    register(name.toString(), extensionClass);
  }

  public T getExtensionProtoType(String name, Object... args) {
    Class<? extends T> extensionClass = extensionClassMap.get(name);
    if (extensionClass == null) {
      throw new NullPointerException("ExtensionClass is null, name: " + name);
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
    T extension = extensionMap.get(name);
    if(extension == null) {
      throw new ExtensionNotFoundException();
    }
    return extension;
  }

  public T getExtensionSingleton(Enum<?> name) {
    return getExtensionSingleton(name.toString());
  }

}
