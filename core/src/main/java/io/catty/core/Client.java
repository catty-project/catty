package io.catty.core;

import io.catty.core.config.InnerClientConfig;

public interface Client extends Endpoint {

  @Override
  InnerClientConfig getConfig();

}
