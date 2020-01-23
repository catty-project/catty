package io.catty.listable;

import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.config.ClientConfig;
import io.catty.core.Client;
import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.InvokerChainBuilder;
import io.catty.core.ListableInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.extension.ExtensionFactory;
import io.catty.extension.ExtensionFactory.InvokerBuilderType;
import io.catty.lbs.LoadBalance;
import io.catty.meta.MetaInfo;
import io.catty.meta.MetaInfoEnum;
import io.catty.service.ServiceMeta;
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

  private Map<MetaInfo, Invoker> invokersMap;

  private ServiceMeta serviceMeta;

  private MetaInfo metaInfo;

  public Cluster(MetaInfo metaInfo, ServiceMeta serviceMeta) {
    this(metaInfo, serviceMeta, new ArrayList<>());
  }

  public Cluster(MetaInfo metaInfo, ServiceMeta serviceMeta, List<Invoker> invokerList) {
    super(invokerList);
    this.metaInfo = metaInfo;
    this.serviceMeta = serviceMeta;
    this.loadBalance = ExtensionFactory.getLoadbalance()
        .getExtension(metaInfo.getString(MetaInfoEnum.LOAD_BALANCE));
    invokersMap = new HashMap<>();
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Invoker invoker = loadBalance.select(invokerList);
    if (!invoker.isAvailable()) {
      invoker.init();
    }
    return invoker.invoke(request, invocation);
  }

  @Override
  public void destroy() {
    invokerList.forEach(Invoker::destroy);
  }

  @Override
  public synchronized void notify(RegistryConfig registryConfig,
      List<MetaInfo> metaInfoCollection) {
    Map<MetaInfo, Invoker> newInvokerMap = new HashMap<>();
    List<Invoker> newInvokerList = new ArrayList<>();

    List<MetaInfo> newList = new ArrayList<>();
    for (MetaInfo metaInfo : metaInfoCollection) {
      if (invokersMap.containsKey(metaInfo)) {
        continue;
      }
      newList.add(metaInfo);
    }
    for (MetaInfo metaInfo : newList) {
      Invoker invoker = createClientFromMetaInfo(metaInfo);
      newInvokerMap.put(metaInfo, invoker);
      newInvokerList.add(invoker);
    }

    Set<MetaInfo> metaInfoSet = new HashSet<>(metaInfoCollection);
    for (Entry<MetaInfo, Invoker> entry : invokersMap.entrySet()) {
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

  private Invoker createClientFromMetaInfo(MetaInfo metaInfo) {
    InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
        .getExtension(InvokerBuilderType.DIRECT);
    ClientConfig clientConfig = ClientConfig.builder()
        .address(buildAddress(metaInfo))
        .build();
    Client client = new NettyClient(clientConfig);
    return chainBuilder.buildConsumerInvoker(metaInfo, client);
  }

  private String buildAddress(MetaInfo metaInfo) {
    return metaInfo.getString(MetaInfoEnum.IP) + ":" + metaInfo.getString(MetaInfoEnum.PORT);
  }
}
