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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import pink.catty.core.invoker.AbstractConsumer;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ConsumerMeta;
import pink.catty.core.service.HealthCheckException;
import pink.catty.core.utils.HeartBeatUtils;

public class ConsumerHealthCheck extends AbstractConsumer {

  private static final String TIMER_NAME = "CATTY_HEARTBEAT";
  private static Timer timer;

  static {
    timer = new Timer(TIMER_NAME);
  }

  private final int period;
  private ConsumerMeta metaInfo;
  private boolean isTimerStart;
  private volatile RuntimeException heartBeatThrowable;
  private AtomicInteger checkErrorRecord;

  public ConsumerHealthCheck(Consumer next) {
    super(next);
    this.metaInfo = next.getMeta();
    this.period = metaInfo.getHealthCheckPeriod();
    this.isTimerStart = false;
    this.checkErrorRecord = new AtomicInteger(0);
  }

  @Override
  public Response invoke(Request request) {
    checkException();
    startTimer();
    return getNext().invoke(request);
  }

  private void startTimer() {
    if (!isTimerStart) {
      synchronized (this) {
        if (!isTimerStart) {
          timer.schedule(new HeartBeatTask(), period, period);
          isTimerStart = true;
        }
      }
    }
  }

  private void checkException() {
    if (heartBeatThrowable != null) {
      RuntimeException t = heartBeatThrowable;
      heartBeatThrowable = null;
      throw t;
    }
  }

  private class HeartBeatTask extends TimerTask {

    @Override
    public void run() {
      try {
        Request request = HeartBeatUtils.buildHeartBeatRequest(ConsumerHealthCheck.this);
        String except = (String) request.getArgsValue()[0];
        Response response = invoke(request);
        try {
          response.await(period, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          // ignore
          return;
        } catch (ExecutionException | TimeoutException e) {
          throw new HealthCheckException("Invoke error", e, ConsumerHealthCheck.this);
        }
        Object returnValue = response.getValue();
        if (returnValue instanceof Throwable) {
          throw new HealthCheckException("Health check error", (Throwable) returnValue,
              ConsumerHealthCheck.this);
        }
        if (!except.equals(returnValue)) {
          throw new HealthCheckException(
              "Health check error, except: " + except + " actually: " + returnValue,
              ConsumerHealthCheck.this);
        }
        checkErrorRecord.set(0);
      } catch (HealthCheckException e) {
        ConsumerHealthCheck.this.heartBeatThrowable = e;
        checkErrorRecord.incrementAndGet();
      } catch (Throwable t) {
        ConsumerHealthCheck.this.heartBeatThrowable = new HealthCheckException(
            "Health check error", t, ConsumerHealthCheck.this);
        checkErrorRecord.incrementAndGet();
      }
    }
  }
}
