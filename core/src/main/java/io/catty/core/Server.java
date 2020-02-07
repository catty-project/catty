package io.catty.core;

import io.catty.core.config.InnerServerConfig;

public interface Server extends Endpoint {

  @Override
  InnerServerConfig getConfig();

}
