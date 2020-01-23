package io.catty.core;

import io.catty.config.ServerConfig;

public interface Server extends Endpoint {

  ServerConfig getConfig();

}
