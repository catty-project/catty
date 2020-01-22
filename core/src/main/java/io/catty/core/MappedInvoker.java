package io.catty.core;

import java.util.Map;

public abstract class MappedInvoker implements Invoker {

  protected Map<Object, Invoker> invokerMap;

  public MappedInvoker(Map<Object, Invoker> invokerMap) {
    this.invokerMap = invokerMap;
  }

  public void setInvokerMap(Map<Object, Invoker> invokerMap) {
    this.invokerMap = invokerMap;
  }

  public void registerInvoker(String serverIdentify, Invoker invoker) {
    invokerMap.put(serverIdentify, invoker);
  }
}
