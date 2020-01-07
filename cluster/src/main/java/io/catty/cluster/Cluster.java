package io.catty.cluster;

import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;
import io.catty.api.Registry.NotifyListener;
import io.catty.config.RegistryConfig;
import io.catty.meta.EndpointMetaInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cluster implements Invoker, NotifyListener {

  private LoadBalance loadBalance;

  private Map<EndpointMetaInfo, Invoker> invokersMap;

  private List<Invoker> invokers;

  private String serverName;

  @Override
  public Response invoke(Request request) {
    return loadBalance.select(invokers).invoke(request);
  }

  @Override
  public synchronized void notify(RegistryConfig registryConfig, List<EndpointMetaInfo> metaInfoCollection) {
    List<EndpointMetaInfo> newList = new ArrayList<>();
    for(EndpointMetaInfo metaInfo : metaInfoCollection) {
      if(invokersMap.containsKey(metaInfo)) {
        continue;
      }

    }
  }
}
