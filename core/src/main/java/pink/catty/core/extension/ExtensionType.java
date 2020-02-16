package pink.catty.core.extension;

public final class ExtensionType {

  public enum SerializationType {
    PROTOBUF,
    FASTJSON,
    PROTOBUF_FASTJSON,
    HESSIAN2,
    ;
  }

  public enum LoadBalanceType {
    RANDOM,
    WEIGHTED_RANDOM,
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

  public enum EndpointFactoryType {
    NETTY,
    ;
  }

}
