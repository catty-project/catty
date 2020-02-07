package io.catty.extension.builder;

import io.catty.core.Client;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.config.InnerClientConfig;
import io.catty.core.extension.Extension;
import io.catty.core.extension.ExtensionFactory;
import io.catty.core.extension.spi.Codec;
import io.catty.core.extension.spi.InvokerChainBuilder;
import io.catty.core.extension.spi.Serialization;
import io.catty.core.meta.MetaInfo;
import io.catty.core.meta.MetaInfoEnum;
import io.catty.linked.ConsumerSerializationInvoker;
import io.catty.linked.ProviderSerializationInvoker;
import io.catty.linked.TimeoutInvoker;
import io.catty.meta.ProviderInvoker;
import io.catty.transport.netty.NettyClient;

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
