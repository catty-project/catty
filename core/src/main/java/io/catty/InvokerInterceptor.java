package io.catty;

public interface InvokerInterceptor {

  void beforeInvoke(Request request, Invocation invocation);

  void afterInvoke(Response response, Invocation invocation);

  enum InterceptorType {
    NORMAL,
    SERIALIZATION,
    ;
  }

}
