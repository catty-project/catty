package io.catty;

import io.catty.Invocation.InvokerLinkTypeEnum;
import io.catty.Response.ResponseStatus;
import io.catty.codec.Serialization;
import io.catty.meta.service.MethodMeta;
import io.catty.meta.service.ServiceMeta;
import io.catty.utils.ExceptionUtils;
import io.catty.utils.RequestIdGenerator;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;


public class ConsumerInvoker<T> implements InvocationHandler {

  private Invoker invoker;
  private Class<T> interfaceClazz;
  private ServiceMeta serviceMeta;
  private Serialization serialization;

  public ConsumerInvoker(Class<T> clazz, Invoker invoker, Serialization serialization) {
    this.interfaceClazz = clazz;
    this.invoker = invoker;
    this.serialization = serialization;
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
    request.setMethodName(method.getName());
    if (args != null && args.length > 0) {
      Object[] argBytes = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
        argBytes[i] = serialization.serialize(args[i]);
      }
      request.setArgsValue(argBytes);
    } else {
      request.setArgsValue(null);
    }

    Class<?> returnType = method.getReturnType();

    Invocation invocation = new Invocation(InvokerLinkTypeEnum.CONSUMER);
    invocation.setInvokedMethod(method);
    Response response = invoker.invoke(request, invocation);

    AsyncResponse asyncResponse = (AsyncResponse) response;

    if (returnType == Void.TYPE) {
      return null;
    }
    // async-method
    // todo : fix exception.
    if (methodMeta.isAsync()) {
      CompletableFuture future = new CompletableFuture();
      asyncResponse.whenComplete((v, t) -> {
        if (t != null) {
          future.completeExceptionally(t);
        } else {
          if (v.isError()) {
            future.completeExceptionally((Exception) v.getValue());
          } else {
            try {
              Type returnGenericType = method.getGenericReturnType();
              future.complete(resolveReturnValue(v.getValue(), returnGenericType));
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
    return resolveReturnValue(response.getValue(), returnType);
  }

  @SuppressWarnings("unchecked")
  private Object resolveReturnValue(Object returnValue, Type resolvedReturnType) {
    if (!(returnValue instanceof byte[])) {
      return returnValue;
    }
    byte[] returnValueBytes = (byte[]) returnValue;
    Class<?> returnType = null;
    if (resolvedReturnType instanceof ParameterizedType) {
      returnType = (Class<?>) ((ParameterizedType) resolvedReturnType)
          .getActualTypeArguments()[0];
    } else if (resolvedReturnType instanceof Class) {
      returnType = (Class<?>) resolvedReturnType;
    }
    if (returnType == null) {
      throw new IllegalArgumentException();
    }
    return serialization.deserialize(returnValueBytes, returnType);
  }

  private boolean isLocalMethod(Method method) {
    if (method.getDeclaringClass().equals(Object.class)) {
      try {
        interfaceClazz
            .getDeclaredMethod(method.getName(), method.getParameterTypes());
        return false;
      } catch (NoSuchMethodException e) {
        return true;
      }
    }
    return false;
  }
}
