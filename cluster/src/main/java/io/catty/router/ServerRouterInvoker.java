package io.catty.router;

import io.catty.Invocation;
import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRouterInvoker implements Invoker {

  private Map<String, Invoker> handlerMap;

  public ServerRouterInvoker() {
    this.handlerMap = new ConcurrentHashMap<>();
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    String serviceName = request.getInterfaceName();
    return handlerMap.getOrDefault(serviceName, DefaultInvoker.INSTANCE).invoke(request, invocation);
  }

  public void registerInvoker(String serverIdentify, Invoker invoker) {
    handlerMap.put(serverIdentify, invoker);
  }

  public static class DefaultInvoker implements Invoker {

    private static Invoker INSTANCE = new DefaultInvoker();

    private DefaultInvoker() {
    }

    @Override
    public Response invoke(Request request, Invocation invocation) {
      throw new UnsupportedOperationException();
    }
  }

}
