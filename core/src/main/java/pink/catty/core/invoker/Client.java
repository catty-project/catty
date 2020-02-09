package pink.catty.core.invoker;

import pink.catty.core.config.InnerClientConfig;

public interface Client extends Endpoint {

  @Override
  InnerClientConfig getConfig();

}
