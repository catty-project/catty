package pink.catty.mapped;

import pink.catty.api.RegistryConfig;
import pink.catty.api.Registry.NotifyListener;
import pink.catty.core.Invocation;
import pink.catty.core.Invoker;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.InvokerHolder;
import pink.catty.core.MappedInvoker;
import pink.catty.core.Request;
import pink.catty.core.Response;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.utils.EndpointUtils;
import pink.catty.core.utils.MetaInfoUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ClusterInvoker extends MappedInvoker implements NotifyListener {

  private LoadBalance loadBalance;

  private ServiceMeta serviceMeta;

  private MetaInfo metaInfo;

  private List<InvokerHolder> invokerList;

  public ClusterInvoker(MetaInfo metaInfo, ServiceMeta serviceMeta) {
    this.metaInfo = metaInfo;
    this.serviceMeta = serviceMeta;
    this.loadBalance = ExtensionFactory.getLoadBalance()
        .getExtensionProtoType(metaInfo.getString(MetaInfoEnum.LOAD_BALANCE));
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    InvokerHolder invokerHolder;
    if(invokerList.size() == 1) {
      invokerHolder = invokerList.get(0);
    } else {
      invokerHolder = loadBalance.select(invokerList);
    }
    invocation.setMetaInfo(invokerHolder.getMetaInfo());
    invocation.setServiceMeta(invokerHolder.getServiceMeta());
    return invokerHolder.getInvoker().invoke(request, invocation);
  }

  @Override
  public void setInvokerMap(Map<String, InvokerHolder> invokerMap) {
    super.setInvokerMap(invokerMap);
    this.invokerList = new ArrayList<>(invokerMap.values());
  }

  public void destroy() {
    invokerList.forEach(invokerHolder -> EndpointUtils.destroyInvoker(invokerHolder.getInvoker()));
  }

  @Override
  public synchronized void notify(RegistryConfig registryConfig,
      List<MetaInfo> metaInfoCollection) {
    metaInfoCollection = findCandidate(metaInfoCollection);

    List<InvokerHolder> newInvokerList = new ArrayList<>();
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
      InvokerHolder invokerHolder = InvokerHolder.Of(metaInfo, serviceMeta, invoker);
      newInvokerList.add(invokerHolder);
      newInvokerMap.put(metaInfo.toString(), invokerHolder);
    }

    // find invalid server.
    Set<String> metaInfoSet = new HashSet<>();
    for (MetaInfo info : metaInfoCollection) {
      metaInfoSet.add(info.toString());
    }
    for (Entry<String, InvokerHolder> entry : invokerMap.entrySet()) {
      if (metaInfoSet.contains(entry.getKey())) {
        newInvokerList.add(entry.getValue());
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
