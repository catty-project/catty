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

import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.AbstractLinkedInvoker;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.RpcTimeoutException;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.support.timer.HashedWheelTimer;
import pink.catty.core.support.timer.Timer;
import java.util.concurrent.TimeUnit;

public class TimeoutInvoker extends AbstractLinkedInvoker {

  private static final String TIMEOUT_MESSAGE = "IP: %s, PORT: %d, INVOKE DETAIL: %s";

  private static Timer timer;

  static {
    timer = new HashedWheelTimer();
  }

  public TimeoutInvoker() {
  }

  public TimeoutInvoker(Invoker next) {
    super(next);
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    int delay = invocation.getInvokedMethod().getTimeout();
    if (delay <= 0) {
      delay = invocation.getServiceMeta().getTimeout();
      if (delay <= 0) {
        return next.invoke(request, invocation);
      }
    }

    Response response = next.invoke(request, invocation);
    timer.newTimeout((timeout) -> {
      if (!response.isDone()) {
        response.setValue(new RpcTimeoutException(buildTimeoutMessage(request, invocation)));
      }
    }, delay, TimeUnit.MILLISECONDS);
    return response;
  }

  private String buildTimeoutMessage(Request request, Invocation invocation) {
    MetaInfo metaInfo = invocation.getMetaInfo();
    return String.format(TIMEOUT_MESSAGE,
        metaInfo.getStringDef(MetaInfoEnum.IP, "0.0.0.0"),
        metaInfo.getIntDef(MetaInfoEnum.PORT, 0),
        request.toString()
    );
  }

}
