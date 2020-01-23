package io.catty.mapped;

import io.catty.CattyException;
import io.catty.DefaultResponse;
import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.InvokerHolder;
import io.catty.core.MappedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Response.ResponseStatus;
import io.catty.service.MethodMeta;
import io.catty.utils.ExceptionUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRouterInvoker extends MappedInvoker {

  public ServerRouterInvoker() {
    this(new ConcurrentHashMap<>());
  }

  public ServerRouterInvoker(Map<Object, InvokerHolder> invokerMap) {
    super(invokerMap);
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    String serviceName = request.getInterfaceName();
    InvokerHolder invokerHolder = invokerMap.get(serviceName);
    if (invokerHolder == null) {
      Response response = new DefaultResponse();
      response.setRequestId(request.getRequestId());
      response.setStatus(ResponseStatus.OUTER_ERROR);
      response.setValue(ExceptionUtils.toString(new CattyException(
          "No such service found! Service name: " + request.getInterfaceName())));
      return response;
    }

    if (invocation == null) {
      invocation = new Invocation(InvokerLinkTypeEnum.PROVIDER);
    }
    invocation.setTarget(invokerHolder.getServiceMeta().getTarget());
    MethodMeta methodMeta = invokerHolder.getServiceMeta()
        .getMethodMetaBySign(request.getMethodName());
    invocation.setInvokedMethod(methodMeta);
    return invokerHolder.getInvoker().invoke(request, invocation);
  }

}
