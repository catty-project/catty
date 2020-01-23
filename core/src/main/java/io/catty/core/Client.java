package io.catty.core;

import io.catty.config.ClientConfig;

public interface Client extends Endpoint {

  @Override
  ClientConfig getConfig();

}
