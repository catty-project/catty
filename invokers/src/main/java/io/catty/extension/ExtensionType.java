package io.catty.extension;

public class ExtensionType {

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

  public enum InvokerBuilderType {
    DIRECT,
    REGISTRY,
    ;
  }

  public enum RegistryType {
    ZOOKEEPER,
    ;
  }


}
