package io.catty;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.catty.api.AsyncResponse;
import io.catty.api.DefaultRequest;
import io.catty.exception.CattyException;
import io.catty.utils.RequestIdGenerator;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;


public class ConsumerInvoker<T> implements InvocationHandler, Invoker<T> {

  private Invoker invoker;

  private Class<T> interfaceClazz;

  public ConsumerInvoker(Class<T> clazz, Invoker invoker) {
    this.interfaceClazz = clazz;
    this.invoker = invoker;
  }

  @Override
  public Class<T> getInterface() {
    return interfaceClazz;
  }

  @Override
  public Response invoke(Request request) {
    return invoker.invoke(request);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (isLocalMethod(method)) {
      throw new CattyException("Can not invoke local method: " + method.getName());
    }

    Request request = new DefaultRequest();
    request.setRequestId(RequestIdGenerator.next());
    request.setInterfaceName(method.getDeclaringClass().getName());
    request.setMethodName(method.getName());
    request.setArgsValue(args);

    Class<?> returnType = method.getReturnType();
    Response response = invoke(request);
    if (returnType == Void.TYPE) {
      return null;
    }

    // async-method
    if (CompletableFuture.class.isAssignableFrom(returnType)) {
      CompletableFuture future = new CompletableFuture();
      AsyncResponse asyncResponse = (AsyncResponse) response;
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
    if (response.isError()) {
      throw (Exception) response.getValue();
    }
    return resolveReturnValue(response.getValue(), returnType);
  }

  @SuppressWarnings("unchecked")
  private Object resolveReturnValue(Object returnValue, Type resolvedReturnType)
      throws IOException {
    if (returnValue instanceof Any && resolvedReturnType instanceof ParameterizedType) {
      return ((Any) returnValue).unpack(
          (Class<? extends Message>) ((ParameterizedType) resolvedReturnType)
              .getActualTypeArguments()[0]);
    } else if (returnValue instanceof Any && Message.class
        .isAssignableFrom((Class<?>) resolvedReturnType)) {
      return ((Any) returnValue).unpack((Class<Message>) resolvedReturnType);
    } else {
      return returnValue;
    }
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
