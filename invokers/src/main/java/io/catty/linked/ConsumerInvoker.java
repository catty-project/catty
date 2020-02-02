package io.catty.linked;

import io.catty.core.CattyException;
import io.catty.core.DefaultRequest;
import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.InvokerHolder;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.service.MethodMeta;
import io.catty.core.service.ServiceMeta;
import io.catty.core.utils.ReflectUtils;
import io.catty.core.utils.RequestIdGenerator;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;


public class ConsumerInvoker<T> extends LinkedInvoker implements InvocationHandler {

  private Class<T> interfaceClazz;
  private ServiceMeta serviceMeta;

  public ConsumerInvoker(Class<T> clazz, InvokerHolder invokerHolder) {
    super(invokerHolder.getInvoker());
    this.interfaceClazz = clazz;
    this.serviceMeta = invokerHolder.getServiceMeta();
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
      // todo: exception need be more accurate.
      throw new CattyException("Method is invalid, method: " + method.getName());
    }

    Request request = new DefaultRequest();
    request.setRequestId(RequestIdGenerator.next());
    request.setInterfaceName(method.getDeclaringClass().getName());
    request.setMethodName(ReflectUtils.getMethodSign(method));
    request.setArgsValue(args);

    Class<?> returnType = method.getReturnType();

    Invocation invocation = new Invocation(InvokerLinkTypeEnum.CONSUMER);
    invocation.setInvokedMethod(methodMeta);
    invocation.setTarget(proxy);

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
          if(v instanceof Throwable && !v.getClass().isAssignableFrom(methodMeta.getReturnType())) {
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
    if(returnValue instanceof Throwable) {
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
  public static <E> E getProxy(Class<E> clazz, InvokerHolder invokerHolder) {
    return (E) Proxy.newProxyInstance(
        clazz.getClassLoader(), new Class[]{clazz}, new ConsumerInvoker(clazz, invokerHolder));
  }
}
