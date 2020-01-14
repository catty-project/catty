package io.catty.meta.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

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
     * NOTICE: Future interface could not indicated an async method.
     */
    if(CompletionStage.class.isAssignableFrom(returnType)) {
      isAsync = true;
    }
  }

}
