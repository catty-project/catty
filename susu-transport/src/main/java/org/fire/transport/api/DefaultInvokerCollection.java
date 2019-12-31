package org.fire.transport.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.fire.core.Invoker;
import org.fire.core.Request;
import org.fire.core.Response;

public class DefaultInvokerCollection implements InvokerCollection {

  private Map<String, Invoker> handlerMap;

  public DefaultInvokerCollection() {
    handlerMap = new ConcurrentHashMap<>();
  }

  @Override
  public void registerInvoker(Invoker invoker) {
    String serverIdentify = invoker.getInterface().getSimpleName();
    handlerMap.put(serverIdentify, invoker);
  }

  @Override
  public Class getInterface() {
    return null;
  }

  @Override
  public Response invoke(Request request) {
    String serviceName = request.getInterfaceName();
    return handlerMap.getOrDefault(serviceName, new DefaultInvoker()).invoke(request);
  }

  public static class DefaultInvoker implements Invoker {

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
