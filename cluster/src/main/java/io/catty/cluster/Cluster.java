package io.catty.cluster;

import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;
import io.catty.api.Registry.NotifyListener;
import io.catty.config.RegistryConfig;

public class Cluster implements Invoker, NotifyListener {

  private LoadBalance loadBalance;

  @Override
  public Response invoke(Request request) {
    return null;
  }

  @Override
  public void notify(RegistryConfig registryConfig, String config) {

  }
}
