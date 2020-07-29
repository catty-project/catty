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
package pink.catty.core.invoker.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.CattyException;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.spi.Protocol;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.invoker.AbstractMappedInvoker;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ClusterMeta;
import pink.catty.core.utils.EndpointUtils;

public abstract class AbstractCluster extends AbstractMappedInvoker<Consumer> implements Cluster {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected LoadBalance loadBalance;
  protected ClusterMeta clusterMeta;

  public AbstractCluster(ClusterMeta clusterMeta) {
    this.clusterMeta = clusterMeta;
    this.loadBalance = ExtensionFactory.getLoadBalance()
        .getExtension(clusterMeta.getLoadBalance());
  }

  @Override
  public ClusterMeta getMeta() {
    return clusterMeta;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Consumer consumer;
    if(invokerList.size() <= 0) {
      throw new CattyException("No valid endpoint. MetaInfo: " + clusterMeta.toString());
    }
    if (invokerList.size() == 1) {
      consumer = invokerList.get(0);
    } else {
      consumer = loadBalance.select(invokerList);
    }
    invocation.setServiceModel(consumer.getMeta().getServiceModel());
    return doInvoke(consumer, request, invocation);
  }

  @Override
  public synchronized void destroy() {
    invokerList.forEach(EndpointUtils::destroyInvoker);
  }

  protected Protocol getChainBuilder() {
    return ExtensionFactory.getProtocol().getExtension(InvokerBuilderType.DIRECT);
  }

  abstract protected Response doInvoke(Consumer consumer, Request request,
      Invocation invocation);
}
