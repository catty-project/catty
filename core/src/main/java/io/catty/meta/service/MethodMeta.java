package io.catty.meta.service;

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
    if(CompletionStage.class.isAssignableFrom(returnType)) {
      isAsync = true;
    }

    resolveReturnTypes(method);
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

}
