package pink.catty.extension.builder;

import pink.catty.core.Client;
import pink.catty.core.Invoker;
import pink.catty.core.LinkedInvoker;
import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.linked.ConsumerSerializationInvoker;
import pink.catty.linked.ProviderSerializationInvoker;
import pink.catty.linked.TimeoutInvoker;
import pink.catty.meta.ProviderInvoker;
import pink.catty.transport.netty.NettyClient;

@Extension("DIRECT")
public class CattyInvokerBuilder implements InvokerChainBuilder {

  @Override
  public Invoker buildConsumerInvoker(MetaInfo metaInfo) {
    String ip = metaInfo.getString(MetaInfoEnum.IP);
    int port = metaInfo.getInt(MetaInfoEnum.PORT);
    InnerClientConfig clientConfig = new InnerClientConfig(ip, port, buildAddress(metaInfo), 0);

    Codec codec = ExtensionFactory.getCodec()
        .getExtensionSingleton(metaInfo.getString(MetaInfoEnum.CODEC));
    Client client = new NettyClient(clientConfig, codec);
    Serialization serialization = ExtensionFactory.getSerialization().getExtensionSingleton(
        metaInfo.getString(MetaInfoEnum.SERIALIZATION));
    LinkedInvoker serializationInvoker = new ConsumerSerializationInvoker(client, serialization);
    LinkedInvoker timeoutInvoker = new TimeoutInvoker(serializationInvoker);
    return timeoutInvoker;
  }

  @Override
  public Invoker buildProviderInvoker(MetaInfo metaInfo) {
    Serialization serialization = ExtensionFactory.getSerialization().getExtensionSingleton(
        metaInfo.getString(MetaInfoEnum.SERIALIZATION));

    ProviderInvoker providerInvoker = new ProviderInvoker();
    LinkedInvoker serializationInvoker = new ProviderSerializationInvoker(providerInvoker, serialization);
    return serializationInvoker;
  }

  private String buildAddress(MetaInfo metaInfo) {
    return metaInfo.getString(MetaInfoEnum.IP) + ":" + metaInfo.getString(MetaInfoEnum.PORT);
  }
}
