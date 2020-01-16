package io.catty;

import io.catty.meta.service.MethodMeta;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Invocation {

  public enum InvokerLinkTypeEnum {
    CONSUMER,
    PROVIDER,
    ;
  }

  private Map<String, Object> attribute = new ConcurrentHashMap<>();

  private MethodMeta invokedMethod;

  private InvokerLinkTypeEnum linkTypeEnum;

  private Object target;

  public Object getTarget() {
    return target;
  }

  public void setTarget(Object target) {
    this.target = target;
  }

  public MethodMeta getInvokedMethod() {
    return invokedMethod;
  }

  public Invocation(InvokerLinkTypeEnum linkTypeEnum) {
    this.linkTypeEnum = linkTypeEnum;
  }

  public void setInvokedMethod(MethodMeta invokedMethod) {
    this.invokedMethod = invokedMethod;
  }

  public InvokerLinkTypeEnum getLinkTypeEnum() {
    return linkTypeEnum;
  }
}
