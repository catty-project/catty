package io.catty.linked;

import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.RpcTimeoutException;
import io.catty.core.meta.MetaInfo;
import io.catty.core.meta.MetaInfoEnum;
import io.catty.core.timer.HashedWheelTimer;
import io.catty.core.timer.Timer;
import java.util.concurrent.TimeUnit;

public class TimeoutInvoker extends LinkedInvoker {

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

  private static final String TIMEOUT_MESSAGE = "IP: %s, PORT: %d, INVOKE DETAIL: %s";

  private String buildTimeoutMessage(Request request, Invocation invocation) {
    MetaInfo metaInfo = invocation.getMetaInfo();
    return String.format(TIMEOUT_MESSAGE,
        metaInfo.getStringDef(MetaInfoEnum.IP, "0.0.0.0"),
        metaInfo.getIntDef(MetaInfoEnum.PORT, 0),
        request.toString()
    );
  }

}
