package io.catty;

public interface InvokerInterceptor {

  void beforeInvoke(Request request, Response response, Invocation invocation);

  void afterInvoke(Request request, Response response, Invocation invocation);

}
