package pink.catty.core;

import pink.catty.core.config.InnerServerConfig;

public interface Server extends Endpoint {

  @Override
  InnerServerConfig getConfig();

}
