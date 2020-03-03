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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import pink.catty.core.CattyException;
import pink.catty.core.EndpointInvalidException;
import pink.catty.core.RpcTimeoutException;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.HealthCheckException;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.utils.HeartBeatUtils;

public class RecoveryCluster extends AbstractClusterInvoker {

  // todo: need configurable
  private static final int DEFAULT_RECOVERY_DELAY = 5 * 1000;
  private static final String TIMER_NAME = "CATTY_RECOVERY";
  private static Timer timer;

  static {
    timer = new Timer(TIMER_NAME);
  }

  public RecoveryCluster(MetaInfo metaInfo, ServiceMeta serviceMeta) {
    super(metaInfo, serviceMeta);
  }

  @Override
  protected Response doInvoke(InvokerHolder invokerHolder, Request request, Invocation invocation) {
    int retryTimes = metaInfo.getIntDef(MetaInfoEnum.RETRY_TIMES, 3);
    int delay = invocation.getInvokedMethod().getTimeout();
    if (delay <= 0) {
      delay = invocation.getServiceMeta().getTimeout();
    }

    Response response = null;
    for (int i = 0; i < Math.max(retryTimes, invokerList.size()); i++) {
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
        unregisterInvoker(invokerHolder.getMetaInfo().toString());
        final MetaInfo metaInfo = invokerHolder.getMetaInfo();
        final ServiceMeta serviceMeta = invokerHolder.getServiceMeta();
        timer.schedule(
            new TimerTask() {
              @Override
              public void run() {
                Invoker invoker = getChainBuilder().buildConsumerInvoker(metaInfo);
                Request heartBeat = HeartBeatUtils.buildHeartBeatRequest();
                String except = (String) heartBeat.getArgsValue()[0];
                Invocation inv = HeartBeatUtils.buildHeartBeatInvocation(this, metaInfo);
                try {
                  Response heartBeatResp = invoker.invoke(heartBeat, inv);
                  heartBeatResp.await();
                  if (except.equals(heartBeatResp.getValue())) {
                    InvokerHolder newHolder = new InvokerHolder(metaInfo, serviceMeta, invoker);
                    registerInvoker(metaInfo.toString(), newHolder);
                    System.out.println("recovery");
                    cancel();
                  }
                } catch (Exception e0) {
                  System.out.println();
                  // ignore todo: maybe log
                }
              }
            }, DEFAULT_RECOVERY_DELAY, DEFAULT_RECOVERY_DELAY);

        if (invokerList.size() > 0) {
          invokerHolder = loadBalance.select(invokerList);
        } else {
          break;
        }
      }
    }
    if (response != null) {
      return response;
    }
    throw new CattyException(
        "RecoveryCluster, after retry: " + retryTimes + ", not found valid endpoint.");
  }
}
