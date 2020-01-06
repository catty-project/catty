package io.catty.router;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;

public class ServerRouterInvoker implements Invoker {

  private Map<String, Invoker> handlerMap;

  public ServerRouterInvoker() {
    this.handlerMap = new ConcurrentHashMap<>();
  }

  @Override
  public Response invoke(Request request) {
    String serviceName = request.getInterfaceName();
    return handlerMap.getOrDefault(serviceName, DefaultInvoker.INSTANCE).invoke(request);
  }

  public void registerInvoker(Invoker invoker) {
    String serverIdentify = invoker.getInterface().getName();
    handlerMap.put(serverIdentify, invoker);
  }

  @Override
  public Class getInterface() {
    return null;
  }

  public static class DefaultInvoker implements Invoker {

    private static Invoker INSTANCE = new DefaultInvoker();

    private DefaultInvoker() {
    }

    @Override
    public Class getInterface() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Response invoke(Request request) {
      throw new UnsupportedOperationException();
    }
  }

}
