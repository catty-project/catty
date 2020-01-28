package io.catty.builder;

import io.catty.codec.Serialization;
import io.catty.config.ClientConfig;
import io.catty.core.Invoker;
import io.catty.core.InvokerChainBuilder;
import io.catty.core.LinkedInvoker;
import io.catty.extension.ExtensionFactory;
import io.catty.linked.SerializationInvoker;
import io.catty.meta.MetaInfo;
import io.catty.meta.MetaInfoEnum;
import io.catty.meta.ProviderInvoker;
import io.catty.core.Client;
import io.catty.transport.netty.NettyClient;

public class CattyInvokerBuilder implements InvokerChainBuilder {

  @Override
  public Invoker buildConsumerInvoker(MetaInfo metaInfo) {
    ClientConfig clientConfig = ClientConfig.builder()
        .address(buildAddress(metaInfo))
        .build();
    Client client = new NettyClient(clientConfig);
    Serialization serialization = ExtensionFactory.getSerialization().getExtensionSingleton(
        metaInfo.getString(MetaInfoEnum.SERIALIZATION));
    LinkedInvoker serializationInvoker = new SerializationInvoker(client, serialization);
    return serializationInvoker;
  }

  @Override
  public Invoker buildProviderInvoker(MetaInfo metaInfo) {
    Serialization serialization = ExtensionFactory.getSerialization().getExtensionSingleton(
        metaInfo.getString(MetaInfoEnum.SERIALIZATION));
    ProviderInvoker providerInvoker = new ProviderInvoker();
    LinkedInvoker serializationInvoker = new SerializationInvoker(providerInvoker, serialization);
    return serializationInvoker;
  }

  private String buildAddress(MetaInfo metaInfo) {
    return metaInfo.getString(MetaInfoEnum.IP) + ":" + metaInfo.getString(MetaInfoEnum.PORT);
  }
}
