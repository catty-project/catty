package pink.catty.invokers.linked;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import pink.catty.core.CattyException;
import pink.catty.core.invoker.DefaultRequest;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.AbstractLinkedInvoker;
import pink.catty.core.invoker.MethodNotFoundException;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.utils.RequestIdGenerator;


public class ConsumerInvoker<T> extends AbstractLinkedInvoker implements InvocationHandler {

  private Class<T> interfaceClazz;
  private ServiceMeta serviceMeta;

  public ConsumerInvoker(ServiceMeta<T> serviceMeta, Invoker invoker) {
    super(invoker);
    this.interfaceClazz = serviceMeta.getInterfaceClass();
    this.serviceMeta = serviceMeta;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    return next.invoke(request, invocation);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    // todo: need better performance.
    if (isLocalMethod(method)) {
      throw new CattyException("Can not invoke local method: " + method.getName());
    }

    MethodMeta methodMeta = serviceMeta.getMethodMeta(method);
    if (methodMeta == null) {
      throw new MethodNotFoundException("Method is invalid, method: " + methodMeta.getName());
    }

    Request request = new DefaultRequest();
    request.setRequestId(RequestIdGenerator.next());
    request.setInterfaceName(serviceMeta.getServiceName());
    request.setMethodName(methodMeta.getName());
    request.setArgsValue(args);

    Class<?> returnType = method.getReturnType();

    Invocation invocation = new Invocation(InvokerLinkTypeEnum.CONSUMER);
    invocation.setInvokedMethod(methodMeta);
    invocation.setTarget(proxy);
    invocation.setServiceMeta(serviceMeta);

    Response response = invoke(request, invocation);

    // todo: If void return will wait a response of TCP need be configurable.
    if (returnType == Void.TYPE) {
      return null;
    }
    // async-method
    if (methodMeta.isAsync()) {
      CompletableFuture future = new CompletableFuture();
      response.whenComplete((v, t) -> {
        if (t != null) {
          future.completeExceptionally(t);
        } else {
          if (v instanceof Throwable
              && !v.getClass().isAssignableFrom(methodMeta.getGenericReturnType())) {
            future.completeExceptionally((Throwable) v);
          } else {
            future.complete(v);
          }
        }
      });
      return future;
    }

    // sync-method
    response.await(); // wait for method return.
    Object returnValue = response.getValue();
    if (returnValue instanceof Throwable
        && !methodMeta.getReturnType().isAssignableFrom(returnValue.getClass())) {
      throw (Throwable) returnValue;
    }
    return response.getValue();
  }

  private boolean isLocalMethod(Method method) {
    if (method.getDeclaringClass().equals(Object.class)) {
      try {
        interfaceClazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        return false;
      } catch (NoSuchMethodException e) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static <E> E getProxy(ServiceMeta serviceMeta, Invoker invoker) {
    Class<E> clazz = serviceMeta.getInterfaceClass();
    return (E) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
        new ConsumerInvoker(serviceMeta, invoker));
  }
}
