package io.catty;

import io.catty.Invocation.InvokerLinkTypeEnum;
import io.catty.meta.service.MethodMeta;
import io.catty.meta.service.ServiceMeta;


public class ServiceInvoker<T> implements Invoker {

  private T ref;
  private Invoker invoker;
  private ServiceMeta serviceMeta;

  /**
   * cache all interface's methods
   */
  public ServiceInvoker(T ref, Class<T> interfaceClazz, Invoker invoker) {
    if (!interfaceClazz.isInterface()) {
      throw new CattyException("ServiceInvoker: interfaceClazz is not a interface!");
    }
    this.ref = ref;
    this.serviceMeta = new ServiceMeta(interfaceClazz);
    this.invoker = invoker;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    if(invocation == null) {
      invocation = new Invocation(InvokerLinkTypeEnum.PROVIDER);
    }
    invocation.setTarget(ref);
    MethodMeta methodMeta = serviceMeta.getMethodMetaBySign(request.getMethodName());
    invocation.setInvokedMethod(methodMeta);
    return invoker.invoke(request, invocation);
  }

}
