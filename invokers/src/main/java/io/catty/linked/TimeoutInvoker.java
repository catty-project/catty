//package io.catty.linked;
//
//import io.catty.DefaultResponse;
//import io.catty.core.Invocation;
//import io.catty.core.Invoker;
//import io.catty.core.LinkedInvoker;
//import io.catty.core.Request;
//import io.catty.core.Response;
//import io.catty.core.Response.ResponseStatus;
//import io.catty.timer.HashedWheelTimer;
//import io.catty.timer.Timer;
//import io.catty.utils.ExceptionUtils;
//import java.util.concurrent.TimeUnit;
//
//public class TimeoutInvoker extends LinkedInvoker {
//
//  private Timer timer;
//
//  public TimeoutInvoker(Invoker next) {
//    super(next);
//    this.timer = new HashedWheelTimer();
//  }
//
//  @Override
//  public Response invoke(Request request, Invocation invocation) {
//    int delay = invocation.getInvokedMethod().getTimeout();
//    if (delay <= 0) {
//      return next.invoke(request, invocation);
//    }
//
//    timer.newTimeout((timeout) -> {
//      Response response = new DefaultResponse(request.getRequestId());
//      response.setStatus(ResponseStatus.TIMEOUT_ERROR);
//      response.setValue(ExceptionUtils.toString(new TimeoutException(request.toString())));
//
//    }, delay, TimeUnit.MILLISECONDS);
//    Response response = next.invoke(request, invocation);
//
//  }
//}
