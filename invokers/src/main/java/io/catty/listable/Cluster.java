package io.catty.listable;

import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.ListableInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.config.ClientConfig;
import io.catty.lbs.LoadBalance;
import io.catty.meta.endpoint.EndpointMetaInfo;
import io.catty.meta.endpoint.MetaInfoEnum;
import io.catty.transport.netty.NettyClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Cluster extends ListableInvoker implements Registry.NotifyListener {

  private LoadBalance loadBalance;

  private Map<EndpointMetaInfo, Invoker> invokersMap;

  public Cluster(LoadBalance loadBalance) {
    this(new ArrayList<>(), loadBalance);
  }

  public Cluster(List<Invoker> invokerList, LoadBalance loadBalance) {
    super(invokerList);
    this.loadBalance = loadBalance;
    invokersMap = new HashMap<>();
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Invoker invoker = loadBalance.select(invokerList);
    if(!invoker.isAvailable()) {
      invoker.init();
    }
    return invoker.invoke(request, invocation);
  }

  public void destroy() {
    invokerList.forEach(Invoker::destroy);
  }

  @Override
  public synchronized void notify(RegistryConfig registryConfig,
      List<EndpointMetaInfo> metaInfoCollection) {
    Map<EndpointMetaInfo, Invoker> newInvokerMap = new HashMap<>();
    List<Invoker> newInvokerList = new ArrayList<>();

    List<EndpointMetaInfo> newList = new ArrayList<>();
    for (EndpointMetaInfo metaInfo : metaInfoCollection) {
      if (invokersMap.containsKey(metaInfo)) {
        continue;
      }
      newList.add(metaInfo);
    }
    for (EndpointMetaInfo metaInfo : newList) {
      Invoker invoker = createClientFromMetaInfo(metaInfo);
      newInvokerMap.put(metaInfo, invoker);
      newInvokerList.add(invoker);
    }

    Set<EndpointMetaInfo> metaInfoSet = new HashSet<>(metaInfoCollection);
    for (Entry<EndpointMetaInfo, Invoker> entry : invokersMap.entrySet()) {
      if (metaInfoSet.contains(entry.getKey())) {
        newInvokerList.add(entry.getValue());
        newInvokerMap.put(entry.getKey(), entry.getValue());
      } else {
        entry.getValue().destroy();
      }
    }
    setInvokerList(newInvokerList);
    invokersMap = newInvokerMap;
  }

  private Invoker createClientFromMetaInfo(EndpointMetaInfo metaInfo) {
    ClientConfig clientConfig = ClientConfig.builder()
        .address(metaInfo.getString(MetaInfoEnum.ADDRESS))
        .build();
    return new NettyClient(clientConfig);
  }
}
