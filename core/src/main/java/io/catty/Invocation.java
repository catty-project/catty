package io.catty;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Invocation {

  private Map<String, Object> attribute = new ConcurrentHashMap<>();

  private Method invokedMethod;

  public Method getInvokedMethod() {
    return invokedMethod;
  }

  public void setInvokedMethod(Method invokedMethod) {
    this.invokedMethod = invokedMethod;
  }
}
