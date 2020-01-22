package io.catty.extension;

import io.catty.codec.CattyCodec;
import io.catty.codec.CattySerialization;
import io.catty.codec.Codec;
import io.catty.codec.Serialization;
import io.catty.lbs.RandomLoadBalance;
import io.catty.lbs.LoadBalance;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExtensionRegistry<T> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private static ExtensionRegistry<Serialization> SERIALIZATION;
  private static ExtensionRegistry<LoadBalance> LOAD_BALANCE;
  private static ExtensionRegistry<Codec> CODEC;

  static {
    SERIALIZATION = new ExtensionRegistry<>();
    LOAD_BALANCE = new ExtensionRegistry<>();
    CODEC = new ExtensionRegistry<>();
    SERIALIZATION.register(SerializationType.PROTOBUF_FASTJSON, new CattySerialization());
    LOAD_BALANCE.register(LoadBalanceType.RANDOM, new RandomLoadBalance());
    CODEC.register(CodecType.CATTY, new CattyCodec());
  }

  public enum SerializationType {
    PROTOBUF,
    FASTJSON,
    PROTOBUF_FASTJSON,
    ;
  }

  public enum LoadBalanceType {
    RANDOM,
    ;
  }

  public enum CodecType {
    CATTY,
    ;
  }

  public static ExtensionRegistry<Serialization> getSerialization() {
    return SERIALIZATION;
  }

  public static ExtensionRegistry<LoadBalance> getLoadbalance() {
    return LOAD_BALANCE;
  }

  private Map<String, T> extensionMap;

  public ExtensionRegistry() {
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
