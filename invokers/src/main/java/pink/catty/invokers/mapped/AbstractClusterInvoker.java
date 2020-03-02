/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.invokers.mapped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.extension.spi.Registry.NotifyListener;
import pink.catty.core.invoker.AbstractMappedInvoker;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.utils.EndpointUtils;
import pink.catty.core.utils.MetaInfoUtils;

public abstract class AbstractClusterInvoker extends AbstractMappedInvoker implements
    NotifyListener {

  protected LoadBalance loadBalance;
  protected ServiceMeta serviceMeta;
  protected MetaInfo metaInfo;
  protected List<InvokerHolder> invokerList;

  public AbstractClusterInvoker(MetaInfo metaInfo, ServiceMeta serviceMeta) {
    this.metaInfo = metaInfo;
    this.serviceMeta = serviceMeta;
    this.loadBalance = ExtensionFactory.getLoadBalance()
        .getExtensionProtoType(metaInfo.getString(MetaInfoEnum.LOAD_BALANCE));
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    InvokerHolder invokerHolder;
    if (invokerList.size() == 1) {
      invokerHolder = invokerList.get(0);
    } else {
      invokerHolder = loadBalance.select(invokerList);
    }
    invocation.setMetaInfo(invokerHolder.getMetaInfo());
    invocation.setServiceMeta(invokerHolder.getServiceMeta());
    return doInvoke(invokerHolder, request, invocation);
  }

  protected abstract Response doInvoke(InvokerHolder invokerHolder, Request request,
      Invocation invocation);

  @Override
  public void setInvokerMap(Map<String, InvokerHolder> invokerMap) {
    super.setInvokerMap(invokerMap);
    this.invokerList = new ArrayList<>(invokerMap.values());
  }

  @Override
  public synchronized InvokerHolder getInvoker(String invokerIdentify) {
    return super.getInvoker(invokerIdentify);
  }

  @Override
  public synchronized void registerInvoker(String serviceIdentify, InvokerHolder invokerHolder) {
    super.registerInvoker(serviceIdentify, invokerHolder);
    invokerList.add(invokerHolder);
  }

  @Override
  public synchronized InvokerHolder unregisterInvoker(String serviceIdentify) {
    InvokerHolder holder = super.unregisterInvoker(serviceIdentify);
    invokerList.remove(holder);
    return holder;
  }

  public synchronized void destroy() {
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
    InvokerChainBuilder chainBuilder = getChainBuilder();
    return chainBuilder.buildConsumerInvoker(metaInfo);
  }

  protected InvokerChainBuilder getChainBuilder() {
    return ExtensionFactory.getInvokerBuilder().getExtensionSingleton(InvokerBuilderType.DIRECT);
  }

}
