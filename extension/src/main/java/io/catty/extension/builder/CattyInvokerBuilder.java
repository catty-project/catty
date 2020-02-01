package io.catty.extension.builder;

import io.catty.core.Client;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.config.ClientConfig;
import io.catty.core.extension.Extension;
import io.catty.core.extension.ExtensionFactory;
import io.catty.core.extension.api.Codec;
import io.catty.core.extension.api.InvokerChainBuilder;
import io.catty.core.extension.api.Serialization;
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
    ClientConfig clientConfig = ClientConfig.builder()
        .address(buildAddress(metaInfo))
        .build();
    Codec codec = ExtensionFactory.getCodec()
        .getExtensionSingleton(metaInfo.getString(MetaInfoEnum.CODEC));
    Client client = new NettyClient(clientConfig, codec);
    Serialization serialization = ExtensionFactory.getSerialization().getExtensionSingleton(
        metaInfo.getString(MetaInfoEnum.SERIALIZATION));
    LinkedInvoker serializationInvoker = new ConsumerSerializationInvoker(client, serialization);
    return serializationInvoker;
  }

  @Override
  public Invoker buildProviderInvoker(MetaInfo metaInfo) {
    Serialization serialization = ExtensionFactory.getSerialization().getExtensionSingleton(
        metaInfo.getString(MetaInfoEnum.SERIALIZATION));

    ProviderInvoker providerInvoker = new ProviderInvoker();
    LinkedInvoker timeoutInvoker = new TimeoutInvoker(providerInvoker);
    LinkedInvoker serializationInvoker = new ProviderSerializationInvoker(timeoutInvoker, serialization);
    return serializationInvoker;
  }

  private String buildAddress(MetaInfo metaInfo) {
    return metaInfo.getString(MetaInfoEnum.IP) + ":" + metaInfo.getString(MetaInfoEnum.PORT);
  }
}
