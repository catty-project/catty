package io.catty.listable;

import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.InvokerChainBuilder;
import io.catty.core.InvokerHolder;
import io.catty.core.MappedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.extension.ExtensionFactory;
import io.catty.extension.ExtensionType.InvokerBuilderType;
import io.catty.lbs.LoadBalance;
import io.catty.meta.MetaInfo;
import io.catty.meta.MetaInfoEnum;
import io.catty.service.ServiceMeta;
import io.catty.utils.EndpointUtils;
import io.catty.utils.MetaInfoUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Cluster extends MappedInvoker implements Registry.NotifyListener {

  private LoadBalance loadBalance;

  private ServiceMeta serviceMeta;

  private MetaInfo metaInfo;

  private List<Invoker> invokerList;

  public Cluster(MetaInfo metaInfo, ServiceMeta serviceMeta) {
    this.metaInfo = metaInfo;
    this.serviceMeta = serviceMeta;
    this.loadBalance = ExtensionFactory.getLoadBalance()
        .getExtensionProtoType(metaInfo.getString(MetaInfoEnum.LOAD_BALANCE));
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Invoker invoker = loadBalance.select(invokerList);
    return invoker.invoke(request, invocation);
  }

  public void destroy() {
    invokerList.forEach(EndpointUtils::destroyInvoker);
  }

  @Override
  public synchronized void notify(RegistryConfig registryConfig,
      List<MetaInfo> metaInfoCollection) {
    metaInfoCollection = findCandidate(metaInfoCollection);

    List<Invoker> newInvokerList = new ArrayList<>();
    Map<String, InvokerHolder> newInvokerMap = new HashMap<>();

    // find new server.
    List<MetaInfo> newList = new ArrayList<>();
    for (MetaInfo metaInfo : metaInfoCollection) {
      if (!invokerMap.containsKey(metaInfo.toString())) {
        newList.add(metaInfo);
      }
    }
    for (MetaInfo metaInfo : newList) {
      Invoker invoker = createClientFromMetaInfo(metaInfo);
      newInvokerList.add(invoker);
      newInvokerMap.put(metaInfo.toString(), InvokerHolder.Of(metaInfo, serviceMeta, invoker));
    }

    // find invalid server.
    Set<String> metaInfoSet = new HashSet<>();
    for (MetaInfo info : metaInfoCollection) {
      metaInfoSet.add(info.toString());
    }
    for (Entry<String, InvokerHolder> entry : invokerMap.entrySet()) {
      if (metaInfoSet.contains(entry.getKey())) {
        newInvokerList.add(entry.getValue().getInvoker());
        newInvokerMap.put(entry.getKey(), entry.getValue());
      } else {
        EndpointUtils.destroyInvoker(entry.getValue().getInvoker());
      }
    }

    this.invokerList = newInvokerList;
    super.invokerMap = newInvokerMap;
  }

  // fixme: Just remove on metaInfoCollection directly might get better performance?
  private List<MetaInfo> findCandidate(List<MetaInfo> metaInfoCollection) {
    List<MetaInfo> newMetaInfo = new ArrayList<>();
    String referenceGroup = metaInfo.getStringDef(MetaInfoEnum.GROUP, "");
    String referenceVersion = metaInfo.getStringDef(MetaInfoEnum.VERSION, "0.0.0");
    for (MetaInfo info : metaInfoCollection) {
      String group = info.getString(MetaInfoEnum.GROUP);
      if (group != null && !group.equals(referenceGroup)) {
        continue;
      }
      String version = info.getString(MetaInfoEnum.VERSION);
      if (!MetaInfoUtils.compareVersion(referenceVersion, version)) {
        continue;
      }
      newMetaInfo.add(info);
    }
    return newMetaInfo;
  }

  private Invoker createClientFromMetaInfo(MetaInfo metaInfo) {
    InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
        .getExtensionSingleton(InvokerBuilderType.DIRECT);
    return chainBuilder.buildConsumerInvoker(metaInfo);
  }

}
