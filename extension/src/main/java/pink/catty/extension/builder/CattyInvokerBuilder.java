package pink.catty.extension.builder;

import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.spi.EndpointFactory;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.invoker.Client;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.LinkedInvoker;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.invokers.linked.ConsumerSerializationInvoker;
import pink.catty.invokers.linked.ProviderSerializationInvoker;
import pink.catty.invokers.linked.TimeoutInvoker;
import pink.catty.invokers.meta.ProviderInvoker;

@Extension("DIRECT")
public class CattyInvokerBuilder implements InvokerChainBuilder {

  @Override
  public Invoker buildConsumerInvoker(MetaInfo metaInfo) {
    String ip = metaInfo.getString(MetaInfoEnum.IP);
    int port = metaInfo.getInt(MetaInfoEnum.PORT);
    String codecType = metaInfo.getString(MetaInfoEnum.CODEC);
    InnerClientConfig clientConfig = new InnerClientConfig(ip, port, buildAddress(metaInfo), 0,
        codecType);

    EndpointFactory factory = ExtensionFactory.getEndpointFactory().getExtensionSingleton(
        metaInfo.getString(MetaInfoEnum.ENDPOINT));

    Client client = factory.createClient(clientConfig);
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
    LinkedInvoker serializationInvoker = new ProviderSerializationInvoker(providerInvoker,
        serialization);
    return serializationInvoker;
  }

  private String buildAddress(MetaInfo metaInfo) {
    return metaInfo.getString(MetaInfoEnum.IP) + ":" + metaInfo.getString(MetaInfoEnum.PORT);
  }
}
