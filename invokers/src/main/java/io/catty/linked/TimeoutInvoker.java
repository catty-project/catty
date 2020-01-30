//package io.catty.linked;
//
//import DefaultResponse;
//import Invocation;
//import Invoker;
//import LinkedInvoker;
//import Request;
//import Response;
//import Response.ResponseStatus;
//import HashedWheelTimer;
//import Timer;
//import ExceptionUtils;
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
