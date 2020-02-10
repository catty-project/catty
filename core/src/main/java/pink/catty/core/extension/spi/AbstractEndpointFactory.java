package pink.catty.core.extension.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.config.InnerServerConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.invoker.Client;
import pink.catty.core.invoker.Server;

public abstract class AbstractEndpointFactory implements EndpointFactory {

  private Map<InnerClientConfig, Client> clientCache = new ConcurrentHashMap<>();
  private Map<InnerServerConfig, Server> serverCache = new ConcurrentHashMap<>();

  @Override
  public Client createClient(InnerClientConfig clientConfig) {
    Client client = clientCache.get(clientConfig);
    if(client == null) {
      Codec codec = ExtensionFactory.getCodec().getExtensionSingleton(clientConfig.getCodecType());
      client = doCreateClient(clientConfig, codec);
    }
    return client;
  }

  @Override
  public Server createServer(InnerServerConfig serverConfig) {
    return null;
  }

  protected abstract Client doCreateClient(InnerClientConfig clientConfig, Codec codec);

}
