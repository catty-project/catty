package io.catty.service;

import io.catty.annotation.Function;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public class MethodMeta {

  private Method method;

  private Map<String, Class<?>> checkedExceptions;

  private boolean isAsync;

  private Class<?> returnType;

  private Class<?> genericReturnType;

  private int timeout = -1; /* -1 means no timeout */

  public MethodMeta(Method method, int timeout) {
    this(method);
    this.timeout = timeout;
  }

  public MethodMeta(Method method) {
    this.method = method;
    this.checkedExceptions = new HashMap<>();
    Arrays.stream(method.getExceptionTypes())
        .forEach(aClass -> checkedExceptions.put(aClass.getName(), aClass));
    this.returnType = method.getReturnType();

    /*
     * If CompletionStage is super-interface of return type, this method is an async method.
     *
     * NOTICE: Future interface could not imply an async method.
     */
    isAsync = CompletionStage.class.isAssignableFrom(returnType);
    resolveReturnTypes(method);

    if (method.isAnnotationPresent(Function.class)) {
      Function function = method.getDeclaredAnnotation(Function.class);
      this.timeout = function.timeout();
    }
  }

  public Class<?> getCheckedExceptionByName(String className) {
    return checkedExceptions.get(className);
  }

  public Method getMethod() {
    return method;
  }

  public boolean isAsync() {
    return isAsync;
  }

  public boolean containsCheckedException(Class<?> exceptionClass) {
    return checkedExceptions.containsKey(exceptionClass.getName());
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public Class<?> getGenericReturnType() {
    return genericReturnType;
  }

  // fixme : bug.
  private void resolveReturnTypes(Method method) {
    Class<?> returnType = method.getReturnType();
    Type genericReturnType = method.getGenericReturnType();
    if (Future.class.isAssignableFrom(returnType)) {
      if (genericReturnType instanceof ParameterizedType) {
        Type actualArgType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        if (actualArgType instanceof ParameterizedType) {
          returnType = (Class<?>) ((ParameterizedType) actualArgType).getRawType();
          genericReturnType = actualArgType;
        } else {
          returnType = (Class<?>) actualArgType;
          genericReturnType = returnType;
        }
      } else {
        returnType = null;
        genericReturnType = null;
      }
    }
    this.returnType = returnType;
    this.genericReturnType = (Class) genericReturnType;
  }

  public int getTimeout() {
    return timeout;
  }
}
