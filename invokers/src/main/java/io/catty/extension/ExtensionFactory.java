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
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExtensionFactory<T> {

  private Logger logger = LoggerFactory.getLogger(getClass());

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
    LOAD_BALANCE.register(LoadBalanceType.RANDOM, new RandomLoadBalance());
    CODEC.register(CodecType.CATTY, new CattyCodec());
    INVOKER_BUILDER.register(InvokerBuilderType.DIRECT, new CattyInvokerBuilder());
  }

  public static ExtensionFactory<Serialization> getSerialization() {
    return SERIALIZATION;
  }

  public static ExtensionFactory<LoadBalance> getLoadbalance() {
    return LOAD_BALANCE;
  }

  public static ExtensionFactory<Codec> getCodec() {
    return CODEC;
  }

  public static ExtensionFactory<InvokerChainBuilder> getInvokerBuilder() {
    return INVOKER_BUILDER;
  }

  private Map<String, T> extensionMap;

  public ExtensionFactory() {
    extensionMap = new HashMap<>();
  }

  public void register(String name, T extension) {
    // todo : if the name present, need a warning.
    extensionMap.put(name, extension);
  }

  public void register(Enum<?> name, T extension) {
    // todo : if the name present, need a warning.
    extensionMap.put(name.toString(), extension);
  }

  public T getExtension(String name) {
    return extensionMap.get(name);
  }

  public T getExtension(Enum<?> name) {
    return extensionMap.get(name.toString());
  }
}
