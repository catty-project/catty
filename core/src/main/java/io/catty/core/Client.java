package io.catty.core;

import io.catty.core.config.ClientConfig;

public interface Client extends Endpoint {

  @Override
  ClientConfig getConfig();

}
