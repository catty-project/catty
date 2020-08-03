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
package pink.catty.extension.cluster;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.ClusterType;
import pink.catty.core.extension.spi.EndpointFactory;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.LinkedInvoker;
import pink.catty.core.invoker.endpoint.Client;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ConsumerMeta;
import pink.catty.core.service.HealthCheckException;
import pink.catty.core.support.ConcurrentHashSet;
import pink.catty.core.utils.EndpointUtils;
import pink.catty.core.utils.HeartBeatUtils;
import pink.catty.invokers.consumer.ConsumerClient;

@Extension(ClusterType.FAIL_BACK)
public class FailBackCluster extends FailOverCluster {

  private static final String TIMER_NAME = "CATTY_RECOVERY";
  private static final Set<ConsumerMeta> ON_RECOVERY;
  private static Timer TIMER;

  static {
    TIMER = new Timer(TIMER_NAME);
    ON_RECOVERY = new ConcurrentHashSet<>();
  }

  @Override
  public Response onError(Consumer invoker, Consumer failedConsumer, Request request,
      RuntimeException e) {

    EndpointUtils.destroyInvoker(failedConsumer);
    final ConsumerMeta failedConsumerMeta = failedConsumer.getMeta();
    final String metaString = failedConsumerMeta.toString();

    int recoveryDelay = failedConsumerMeta.getRecoveryPeriod();

    synchronized (ON_RECOVERY) {

      /*
       * Avoid duplicate recovery job.
       */
      if (ON_RECOVERY.contains(failedConsumerMeta)) {
        logger.debug(
            "Recovery job on this address was going on, new recovery job on this address would not be created again. meta: {}",
            metaString);
      } else {
        TIMER.schedule(
            new RecoveryTask(recoveryDelay, metaString, failedConsumerMeta, failedConsumer),
            recoveryDelay, recoveryDelay);
        ON_RECOVERY.add(failedConsumerMeta);
      }
    }
    return super.onError(invoker, failedConsumer, request, e);
  }

  private static class RecoveryTask extends TimerTask {

    private static Logger logger = LoggerFactory.getLogger(RecoveryTask.class);

    private final int period;
    private final String metaString;
    private final ConsumerMeta failedConsumerMeta;
    private final Consumer failedConsumer;

    public RecoveryTask(int period, String metaString,
        ConsumerMeta failedConsumerMeta, Consumer failedConsumer) {
      this.period = period;
      this.metaString = metaString;
      this.failedConsumerMeta = failedConsumerMeta;
      this.failedConsumer = failedConsumer;
    }

    @Override
    public void run() {
      /*
       * 1. create a new invoker from provider info.
       * 2. fire a heartbeat to endpoint, which will attempt to connect to endpoint.
       * 3. if heartbeat succeed, register this invoker.
       * 4. cancel this task.
       */
      logger.info("Recovery: begin recovery of endpoint: {}", metaString);

      try {
        EndpointFactory endpointFactory = ExtensionFactory.endpointFactory()
            .getExtension(failedConsumerMeta.getEndpoint());
        Client newClient = endpointFactory.getClient(failedConsumerMeta);

        Invoker next = failedConsumer;
        while (next instanceof LinkedInvoker) {
          if (((LinkedInvoker) next).getNext() instanceof ConsumerClient) {
            ConsumerClient newConsumerClient = new ConsumerClient(newClient,
                failedConsumerMeta);
            ((LinkedInvoker) next).setNext(newConsumerClient);
            break;
          } else {
            next = ((LinkedInvoker) next).getNext();
          }
        }

        Request heartBeat = HeartBeatUtils.buildHeartBeatRequest(this);
        String except = (String) heartBeat.getArgsValue()[0];
        Response heartBeatResp = failedConsumer.invoke(heartBeat);
        heartBeatResp.await(period, TimeUnit.MILLISECONDS);
        if (except.equals(heartBeatResp.getValue())) {
          logger.info("Recovery: endpoint recovery succeed! endpoint: {}", metaString);
          ON_RECOVERY.remove(failedConsumerMeta);
          cancel();
        } else {
          throw new HealthCheckException(
              "Recovery: excepted: " + except + ", get: " + heartBeatResp.getValue());
        }
      } catch (Exception e0) {
        logger.info(
            "Recovery: endpoint recovery failed, another try is going to begin, endpoint: {}",
            metaString, e0);
      }
    }
  }
}
