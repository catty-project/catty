package org.fire.transport.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRoutableHandler implements RoutableHandler {

  private Map<String, Handler> handlerMap;

  @Override
  public List<Handler> getAllHandler() {
    return new ArrayList<>(handlerMap.values());
  }

  public DefaultRoutableHandler() {
    handlerMap = new ConcurrentHashMap<>();
  }

  @Override
  public void registerHandler(Handler handler) {
    String serverIdentify = handler.getServiceName();
    handlerMap.put(serverIdentify, handler);
  }

  @Override
  public void merge(RoutableHandler handler) {
    List<Handler> handlers = handler.getAllHandler();
    if (handlers != null && handlers.size() > 0) {
      for (Handler h : handlers) {
        if (h instanceof RoutableHandler) {
          merge((RoutableHandler) h);
        } else {
          registerHandler(h);
        }
      }
    }
  }

  @Override
  public Object handle(Object message) {
    String serviceName = ((Request) message).getInterfaceName();
    return handlerMap.getOrDefault(serviceName, new DefaultHandler()).handle(message);
  }

  public static class DefaultHandler implements Handler {

    @Override
    public String getServiceName() {
      return null;
    }

    @Override
    public Object handle(Object message) {
      throw new UnsupportedOperationException();
    }
  }
}
