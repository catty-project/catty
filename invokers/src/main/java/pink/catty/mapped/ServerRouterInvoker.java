package pink.catty.mapped;

import pink.catty.core.CattyException;
import pink.catty.core.Invocation;
import pink.catty.core.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.InvokerHolder;
import pink.catty.core.MappedInvoker;
import pink.catty.core.Request;
import pink.catty.core.Response;
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
        .getMethodMetaBySign(request.getMethodName());
    invocation.setInvokedMethod(methodMeta);
    invocation.setServiceMeta(invokerHolder.getServiceMeta());
    invocation.setMetaInfo(invokerHolder.getMetaInfo());
    return invokerHolder.getInvoker().invoke(request, invocation);
  }

}
