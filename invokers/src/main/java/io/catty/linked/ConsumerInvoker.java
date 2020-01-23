package io.catty.linked;

import io.catty.AsyncResponse;
import io.catty.CattyException;
import io.catty.DefaultRequest;
import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.InvokerHolder;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Response.ResponseStatus;
import io.catty.service.MethodMeta;
import io.catty.service.ServiceMeta;
import io.catty.utils.ExceptionUtils;
import io.catty.utils.ReflectUtils;
import io.catty.utils.RequestIdGenerator;
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

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (isLocalMethod(method)) {
      throw new CattyException("Can not invoke local method: " + method.getName());
    }

    MethodMeta methodMeta = serviceMeta.getMethodMeta(method);
    if (methodMeta == null) {
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

    AsyncResponse asyncResponse = (AsyncResponse) response;

    if (returnType == Void.TYPE) {
      return null;
    }
    // async-method
    if (methodMeta.isAsync()) {
      CompletableFuture future = new CompletableFuture();
      asyncResponse.whenComplete((v, t) -> {
        if (t != null) {
          future.completeExceptionally(t);
        } else {
          if (v.isError()) {
            String[] exceptionInfo = ExceptionUtils
                .parseExceptionString((String) response.getValue());
            if (response.getStatus() == ResponseStatus.EXCEPTED_ERROR) {
              future.completeExceptionally(ExceptionUtils
                  .getInstance(methodMeta.getCheckedExceptionByName(exceptionInfo[0]),
                      exceptionInfo[1]));
            } else {
              future.completeExceptionally(new CattyException(exceptionInfo[1]));
            }
          } else {
            try {
              future.complete(v.getValue());
            } catch (Exception e) {
              future.completeExceptionally(e);
            }
          }
        }
      });
      return future;
    }

    // sync-method
    asyncResponse.await();
    if (response.isError()) {
      String[] exceptionInfo = ExceptionUtils.parseExceptionString((String) response.getValue());
      if (response.getStatus() == ResponseStatus.EXCEPTED_ERROR) {
        throw ExceptionUtils
            .getInstance(methodMeta.getCheckedExceptionByName(exceptionInfo[0]), exceptionInfo[1]);
      }
      throw new CattyException(exceptionInfo[1]);
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
