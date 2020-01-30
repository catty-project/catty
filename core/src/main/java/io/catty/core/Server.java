package io.catty.core;

import io.catty.core.config.ServerConfig;

public interface Server extends Endpoint {

  @Override
  ServerConfig getConfig();

}
