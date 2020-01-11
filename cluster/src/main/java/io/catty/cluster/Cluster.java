package io.catty.cluster;

import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;
import io.catty.Runtime;
import io.catty.api.Client;
import io.catty.api.Registry;
import io.catty.config.ClientConfig;
import io.catty.api.RegistryConfig;
import io.catty.meta.EndpointMetaInfo;
import io.catty.meta.MetaInfoEnum;
import io.catty.netty.NettyClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Cluster implements Invoker, Registry.NotifyListener {

  private LoadBalance loadBalance;

  private Map<EndpointMetaInfo, Client> invokersMap;

  private List<Client> invokers;

  public Cluster(LoadBalance loadBalance) {
    this.loadBalance = loadBalance;
    invokersMap = new HashMap<>();
    invokers = new ArrayList<>();
  }

  @Override
  public Response invoke(Request request, Runtime runtime) {
    return loadBalance.select(invokers).invoke(request, runtime);
  }

  public void close() {
    invokers.forEach(Client::close);
  }

  @Override
  public synchronized void notify(RegistryConfig registryConfig,
      List<EndpointMetaInfo> metaInfoCollection) {
    Map<EndpointMetaInfo, Client> newInvokerMap = new HashMap<>();
    List<Client> newInvokerList = new ArrayList<>();

    List<EndpointMetaInfo> newList = new ArrayList<>();
    for (EndpointMetaInfo metaInfo : metaInfoCollection) {
      if (invokersMap.containsKey(metaInfo)) {
        continue;
      }
      newList.add(metaInfo);
    }
    for (EndpointMetaInfo metaInfo : newList) {
      Client client = createClientFromMetaInfo(metaInfo);
      client.open();
      newInvokerMap.put(metaInfo, client);
      newInvokerList.add(client);
    }

    Set<EndpointMetaInfo> metaInfoSet = new HashSet<>(metaInfoCollection);
    for (Entry<EndpointMetaInfo, Client> entry : invokersMap.entrySet()) {
      if (metaInfoSet.contains(entry.getKey())) {
        newInvokerList.add(entry.getValue());
        newInvokerMap.put(entry.getKey(), entry.getValue());
      } else {
        entry.getValue().close();
      }
    }
    invokers = newInvokerList;
    invokersMap = newInvokerMap;
  }

  private Client createClientFromMetaInfo(EndpointMetaInfo metaInfo) {
    ClientConfig clientConfig = ClientConfig.builder()
        .address(metaInfo.getString(MetaInfoEnum.ADDRESS.toString()))
        .build();
    return new NettyClient(clientConfig);
  }
}
