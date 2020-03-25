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
package pink.catty.invokers.cluster;

import java.util.Timer;
import java.util.TimerTask;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.utils.HeartBeatUtils;

public class RecoveryCluster extends FailOverCluster {

  private static final String TIMER_NAME = "CATTY_RECOVERY";
  private static Timer timer;

  static {
    timer = new Timer(TIMER_NAME);
  }

  private int defaultRecoveryDelay;

  public RecoveryCluster(MetaInfo metaInfo, ServiceMeta serviceMeta) {
    super(metaInfo, serviceMeta);
    this.defaultRecoveryDelay = metaInfo.getIntDef(MetaInfoEnum.RECOVERY_PERIOD, 3 * 1000);
  }

  @Override
  protected void processError(InvokerHolder invokerHolder, Request request, Invocation invocation,
      Throwable e) {

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
                cancel();
              }
            } catch (Exception e0) {
              // ignore todo: maybe log
            }
          }
        }, defaultRecoveryDelay, defaultRecoveryDelay);
  }
}
