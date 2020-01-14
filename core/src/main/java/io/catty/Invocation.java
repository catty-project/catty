package io.catty;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Invocation {

  public enum InvokerLinkTypeEnum {
    CONSUMER,
    PROVIDER,
    ;
  }

  private Map<String, Object> attribute = new ConcurrentHashMap<>();

  private Method invokedMethod;

  private InvokerLinkTypeEnum linkTypeEnum;

  public Method getInvokedMethod() {
    return invokedMethod;
  }

  public Invocation(InvokerLinkTypeEnum linkTypeEnum) {
    this.linkTypeEnum = linkTypeEnum;
  }

  public void setInvokedMethod(Method invokedMethod) {
    this.invokedMethod = invokedMethod;
  }

  public InvokerLinkTypeEnum getLinkTypeEnum() {
    return linkTypeEnum;
  }
}
