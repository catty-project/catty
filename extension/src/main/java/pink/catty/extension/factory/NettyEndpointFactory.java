package pink.catty.extension.factory;

import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.AbstractEndpointFactory;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.invoker.Client;
import pink.catty.invokers.endpoint.NettyClient;

@Extension("NETTY")
public class NettyEndpointFactory extends AbstractEndpointFactory {

  @Override
  protected Client doCreateClient(InnerClientConfig clientConfig, Codec codec) {
    return new NettyClient(clientConfig, codec);
  }
}
