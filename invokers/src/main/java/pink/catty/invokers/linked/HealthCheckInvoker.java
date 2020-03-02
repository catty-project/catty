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
package pink.catty.invokers.linked;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import pink.catty.core.Constants;
import pink.catty.core.invoker.AbstractLinkedInvoker;
import pink.catty.core.invoker.DefaultRequest;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.HealthCheckException;
import pink.catty.core.service.HeartBeatService;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.utils.RequestIdGenerator;

public class HealthCheckInvoker extends AbstractLinkedInvoker {

  private static final int DEFAULT_HEALTH_CHECK_PERIOD = 10 * 1000; // 30s
  private static final String TIMER_NAME = "CATTY_HEARTBEAT";
  private static ServiceMeta<HeartBeatService> heartBeatServiceMeta;
  private static MethodMeta methodMeta;
  private static Timer timer;

  static {
    heartBeatServiceMeta = ServiceMeta.parse(HeartBeatService.class);
    methodMeta = heartBeatServiceMeta.getMethodMetaByName(Constants.HEARTBEAT_METHOD_NAME);
    timer = new Timer(TIMER_NAME);
  }

  private final int period; // 30s
  private MetaInfo metaInfo;
  private boolean isTimerStart;
  private volatile RuntimeException heartBeatThrowable;
  private AtomicInteger checkErrorRecord;

  public HealthCheckInvoker(MetaInfo metaInfo) {
    this.metaInfo = metaInfo;
    this.period = metaInfo.getIntDef(MetaInfoEnum.HEALTH_CHECK_PERIOD, DEFAULT_HEALTH_CHECK_PERIOD);
    this.isTimerStart = false;
    this.checkErrorRecord = new AtomicInteger(0);
  }

  public HealthCheckInvoker(Invoker next, MetaInfo metaInfo) {
    super(next);
    this.period = metaInfo.getIntDef(MetaInfoEnum.HEALTH_CHECK_PERIOD, DEFAULT_HEALTH_CHECK_PERIOD);
    this.metaInfo = metaInfo;
    this.isTimerStart = false;
    this.checkErrorRecord = new AtomicInteger(0);
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    checkException();
    startTimer();
    return getNext().invoke(request, invocation);
  }

  private void startTimer() {
    if (!isTimerStart) {
      synchronized (this) {
        if (!isTimerStart) {
          timer.schedule(new HeartBeatTask(), 0, period);
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
        String arg = UUID.randomUUID().toString();
        Object[] args = {arg};
        Request request = new DefaultRequest(RequestIdGenerator.next(),
            heartBeatServiceMeta.getServiceName(), methodMeta.getName(), args);
        Invocation invocation = new Invocation(InvokerLinkTypeEnum.CONSUMER);
        invocation.setMetaInfo(metaInfo);
        invocation.setTarget(HealthCheckInvoker.this);
        invocation.setServiceMeta(heartBeatServiceMeta);
        invocation.setInvokedMethod(methodMeta);
        Response response = invoke(request, invocation);
        try {
          response.await();
        } catch (InterruptedException e) {
          // ignore
          return;
        } catch (ExecutionException e) {
          throw new HealthCheckException("Invoke error", e, HealthCheckInvoker.this);
        }
        Object returnValue = response.getValue();
        if (returnValue instanceof Throwable) {
          throw new HealthCheckException("Health check error", (Throwable) returnValue,
              HealthCheckInvoker.this);
        }
        if (!arg.equals(returnValue)) {
          throw new HealthCheckException(
              "Health check error, except: " + arg + " actually: " + returnValue,
              HealthCheckInvoker.this);
        }
        checkErrorRecord.set(0);
      } catch (HealthCheckException e) {
        HealthCheckInvoker.this.heartBeatThrowable = e;
        checkErrorRecord.incrementAndGet();
      } catch (Throwable t) {
        HealthCheckInvoker.this.heartBeatThrowable = new HealthCheckException(
            "Health check error", t, HealthCheckInvoker.this);
        checkErrorRecord.incrementAndGet();
      }
    }
  }
}
