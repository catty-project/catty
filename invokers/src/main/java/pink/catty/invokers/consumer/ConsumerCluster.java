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
package pink.catty.invokers.consumer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.CattyException;
import pink.catty.core.RpcTimeoutException;
import pink.catty.core.extension.spi.Cluster;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.invoker.AbstractConsumer;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ConsumerMeta;
import pink.catty.core.support.timer.HashedWheelTimer;
import pink.catty.core.support.timer.Timer;

public class ConsumerCluster extends AbstractConsumer {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerCluster.class);

  private static final String FAILED_INVOKER = "$FAILED_INVOKER$";
  private static final int MAX_TIMEOUT = 30 * 1000; // 30s

  private final Cluster cluster;
  private final LoadBalance loadBalance;
  private final ConsumerMeta consumerMeta;
  private Timer timer;

  public ConsumerCluster(ConsumerMeta consumerMeta, Cluster cluster, LoadBalance loadBalance) {
    super(null);
    this.consumerMeta = consumerMeta;
    this.cluster = cluster;
    this.loadBalance = loadBalance;
    this.timer = new HashedWheelTimer();
  }

  @Override
  public ConsumerMeta getMeta() {
    return this.consumerMeta;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Response invoke(Request request) {
    Object failedInvokers = request.getAttribute(FAILED_INVOKER);
    if (failedInvokers == null) {
      failedInvokers = new HashSet<Consumer>();
      request.addAttribute(FAILED_INVOKER, failedInvokers);
    }

    List<Consumer> candidates = new LinkedList<>();
    for (Consumer consumer : cluster.listConsumer()) {
      if (!((HashSet<Consumer>) failedInvokers).contains(consumer)) {
        candidates.add(consumer);
      }
    }

    // check if there is no candidate.
    if (candidates.size() == 0) {
      logger.error("ConsumerCluster, no valid endpoint. meta: {}", getMeta());
      throw new CattyException(
          "ConsumerCluster, no valid endpoint. meta: " + getMeta());
    }

    // select one consumer from candidates.
    Consumer consumer = loadBalance.select(candidates);

    Response response;
    try {
      // if time out.

      int timeout = min(consumer.getMeta().getTimeout(),
          request.getServiceModel().getTimeout(),
          request.getInvokedMethod().getTimeout()
      );
      if (timeout <= 0) {
        timeout = MAX_TIMEOUT;
      }

      response = consumer.invoke(request);

      if (request.getInvokedMethod().isAsync()) {
        Response finalResponse = response;
        timer.newTimeout((t) -> {
          if (!finalResponse.isDone()) {
            finalResponse.setValue(new RpcTimeoutException());
          }
        }, timeout, TimeUnit.MILLISECONDS);
      } else if (request.getInvokedMethod().isNeedReturn()) {
        try {
          response.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
          logger.error("ConsumerCluster: time wait error", e);
          throw new RpcTimeoutException(e);
        }
      }
    } catch (RuntimeException e) {
      ((HashSet<Consumer>) failedInvokers).add(consumer);
      response = cluster.onError(this, consumer, request, e);
    }

    return response;
  }

  private int min(int... options) {
    int min = Integer.MAX_VALUE;
    for (int i : options) {
      if (i > 0 && i < min) {
        min = i;
      }
    }
    return min;
  }
}


