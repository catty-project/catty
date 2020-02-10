package pink.catty.core.extension.spi;

import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.config.InnerServerConfig;
import pink.catty.core.invoker.Client;
import pink.catty.core.invoker.Server;

public interface EndpointFactory {

  Client createClient(InnerClientConfig clientConfig);

  Server createServer(InnerServerConfig serverConfig);

}
