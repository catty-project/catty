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
import pink.catty.core.invoker.cluster.AbstractClusterInvoker;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.HealthCheckException;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.utils.EndpointUtils;

public class FailOverCluster extends AbstractClusterInvoker {

  public FailOverCluster(MetaInfo metaInfo,
      ServiceMeta serviceMeta) {
    super(metaInfo, serviceMeta);
  }

  @Override
  protected Response doInvoke(InvokerHolder invokerHolder, Request request, Invocation invocation) {
    int retryTimes = metaInfo.getIntDef(MetaInfoEnum.RETRY_TIMES, 1);
    int delay = invocation.getInvokedMethod().getTimeout();
    if (delay <= 0) {
      delay = invocation.getServiceMeta().getTimeout();
    }

    Response response = null;
    for (int i = 0; i <= Math.max(retryTimes, invokerList.size()); i++) {
      try {
        response = invokerHolder.getInvoker().invoke(request, invocation);
        if (delay >= 0) {
          try {
            response.await(delay, TimeUnit.MILLISECONDS);
          } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new HealthCheckException("Invoke error", e, null);
          }
        }
        break;
      } catch (HealthCheckException | EndpointInvalidException | RpcTimeoutException e) {
        logger.error(
            "Cluster: endpoint broken, endpoint provider info: {}, this endpoint will be remove from cluster candidate.",
            invokerHolder.getMetaInfo().toString(), e);
        unregisterInvoker(invokerHolder.getMetaInfo().toString());
        EndpointUtils.destroyInvoker(invokerHolder.getInvoker());
        processError(invokerHolder, request, invocation, e);
        if (invokerList.size() > 0) {
          invokerHolder = loadBalance.select(invokerList);
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

  protected void processError(InvokerHolder invokerHolder, Request request, Invocation invocation,
      Throwable e) {

  }
}
