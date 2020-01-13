package io.catty.meta.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
    if(Future.class.isAssignableFrom(returnType)) {
      isAsync = true;
    }
  }

}
