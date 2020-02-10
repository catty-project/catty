package pink.catty.invokers.mapped;

import pink.catty.core.CattyException;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.invoker.MappedInvoker;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.service.MethodMeta;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRouterInvoker extends MappedInvoker {

  public ServerRouterInvoker() {
    super(new ConcurrentHashMap<>());
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    String serviceName = request.getInterfaceName();
    InvokerHolder invokerHolder = invokerMap.get(serviceName);
    if (invokerHolder == null) {
      throw new CattyException(
          "No such service found! Service name: " + request.getInterfaceName());
    }

    if (invocation == null) {
      invocation = new Invocation(InvokerLinkTypeEnum.PROVIDER);
    }
    invocation.setTarget(invokerHolder.getServiceMeta().getTarget());
    MethodMeta methodMeta = invokerHolder.getServiceMeta()
        .getMethodMetaByName(request.getMethodName());
    invocation.setInvokedMethod(methodMeta);
    invocation.setServiceMeta(invokerHolder.getServiceMeta());
    invocation.setMetaInfo(invokerHolder.getMetaInfo());
    return invokerHolder.getInvoker().invoke(request, invocation);
  }

}
