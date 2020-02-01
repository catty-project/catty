package io.catty.mapped;

import io.catty.core.CattyException;
import io.catty.core.DefaultResponse;
import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.InvokerHolder;
import io.catty.core.MappedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.service.MethodMeta;
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
      Response response = new DefaultResponse(request.getRequestId());
      response.setValue(
          new CattyException("No such service found! Service name: " + request.getInterfaceName()));
      return response;
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
