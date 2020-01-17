package io.catty;

import io.catty.Invocation.InvokerLinkTypeEnum;
import io.catty.Response.ResponseStatus;
import io.catty.meta.service.MethodMeta;
import io.catty.meta.service.ServiceMeta;
import io.catty.utils.ExceptionUtils;
import io.catty.utils.ReflectUtils;
import io.catty.utils.RequestIdGenerator;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;


public class ConsumerInvoker<T> implements InvocationHandler {

  private Invoker invoker;
  private Class<T> interfaceClazz;
  private ServiceMeta serviceMeta;

  public ConsumerInvoker(Class<T> clazz, Invoker invoker) {
    this.interfaceClazz = clazz;
    this.invoker = invoker;
    this.serviceMeta = new ServiceMeta(clazz);
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
    invocation.setInvokedMethod(new MethodMeta(method));
    invocation.setTarget(proxy);
    Response response = invoker.invoke(request, invocation);

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
}
