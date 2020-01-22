package io.catty.linked;

import io.catty.CattyException;
import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.meta.service.MethodMeta;
import io.catty.meta.service.ServiceMeta;


public class ServiceInvoker<T> extends LinkedInvoker {

  private T ref;
  private ServiceMeta serviceMeta;

  /**
   * cache all interface's methods
   */
  public ServiceInvoker(T ref, Class<T> interfaceClazz, Invoker invoker) {
    super(invoker);
    if (!interfaceClazz.isInterface()) {
      throw new CattyException("ServiceInvoker: interfaceClazz is not a interface!");
    }
    this.ref = ref;
    this.serviceMeta = ServiceMeta.parse(interfaceClazz);
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    if(invocation == null) {
      invocation = new Invocation(InvokerLinkTypeEnum.PROVIDER);
    }
    invocation.setTarget(ref);
    MethodMeta methodMeta = serviceMeta.getMethodMetaBySign(request.getMethodName());
    invocation.setInvokedMethod(methodMeta);
    return next.invoke(request, invocation);
  }

}
