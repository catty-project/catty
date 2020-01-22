package io.catty.mapped;

import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.MappedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRouterInvoker extends MappedInvoker {

  public ServerRouterInvoker() {
    this(new ConcurrentHashMap<>());
  }

  public ServerRouterInvoker(Map<Object, Invoker> invokerMap) {
    super(invokerMap);
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    String serviceName = request.getInterfaceName();
    return invokerMap.getOrDefault(serviceName, DefaultInvoker.INSTANCE).invoke(request, invocation);
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
