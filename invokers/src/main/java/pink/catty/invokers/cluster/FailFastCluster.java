/*
 * Copyright 2020 The Catty Project
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
package pink.catty.invokers.cluster;

import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.cluster.AbstractClusterInvoker;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ClusterMeta;

public class FailFastCluster extends AbstractClusterInvoker {

  public FailFastCluster(ClusterMeta clusterMeta) {
    super(clusterMeta);
  }

  @Override
  protected Response doInvoke(Consumer consumer, Request request, Invocation invocation) {
    return consumer.invoke(request, invocation);
  }
}
