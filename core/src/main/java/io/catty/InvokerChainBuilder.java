package io.catty;

import java.util.LinkedList;
import java.util.List;

public class InvokerChainBuilder {

  private List<InvokerInterceptor> interceptors = new LinkedList<>();

  private Invoker sourceInvoker;

  public void setSourceInvoker(Invoker invoker) {
    this.sourceInvoker = invoker;
  }

  public void registerInterceptor(InvokerInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public Invoker buildInvoker() {
    return (request, invocation) -> {
      interceptors.forEach(interceptor -> interceptor.beforeInvoke(request, invocation));
      Response response = sourceInvoker.invoke(request, invocation);
      interceptors.forEach(interceptor -> interceptor.afterInvoke(response, invocation));
      return response;
    };
  }
}
