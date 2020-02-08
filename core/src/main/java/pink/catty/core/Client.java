package pink.catty.core;

import pink.catty.core.config.InnerClientConfig;

public interface Client extends Endpoint {

  @Override
  InnerClientConfig getConfig();

}
