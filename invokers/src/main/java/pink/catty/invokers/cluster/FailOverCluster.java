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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import pink.catty.core.CattyException;
import pink.catty.core.EndpointInvalidException;
import pink.catty.core.RpcTimeoutException;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.cluster.AbstractCluster;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ClusterMeta;
import pink.catty.core.service.HealthCheckException;
import pink.catty.core.utils.EndpointUtils;

public class FailOverCluster extends AbstractCluster {

  public FailOverCluster(ClusterMeta clusterMeta) {
    super(clusterMeta);
  }

  @Override
  protected Response doInvoke(Consumer consumer, Request request, Invocation invocation) {
    int retryTimes = clusterMeta.getRetryTimes();
    int delay = invocation.getInvokedMethod().getTimeout();
    if (delay <= 0) {
      delay = invocation.getServiceMeta().getTimeout();
    }

    Response response = null;
    for (int i = 0; i <= Math.max(retryTimes, invokerList.size()); i++) {
      try {
        response = consumer.invoke(request, invocation);
        if (delay >= 0) {
          try {
            response.await(delay, TimeUnit.MILLISECONDS);
          } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new HealthCheckException("Invoke error", e, null);
          }
        }
        break;
      } catch (HealthCheckException | EndpointInvalidException | RpcTimeoutException e) {
        String metaString = consumer.getMeta().toString();
        logger.error(
            "Cluster: endpoint broken, endpoint provider info: {}, this endpoint will be remove from cluster candidate.",
            metaString, e);
        unregisterInvoker(metaString);
        EndpointUtils.destroyInvoker(consumer);
        processError(consumer, request, invocation, e);
        if (invokerList.size() > 0) {
          consumer = loadBalance.select(invokerList);
        }
      }
    }
    if (response != null) {
      return response;
    }
    logger.error("RecoveryCluster, after retry: {}, not found valid endpoint.", retryTimes);
    throw new CattyException(
        "RecoveryCluster, after retry: " + retryTimes + ", not found valid endpoint.");
  }

  protected void processError(Consumer consumer, Request request, Invocation invocation,
      Throwable e) {

  }
}
