package io.catty.core;

import java.util.Map;

public abstract class MappedInvoker implements Invoker {

  protected Map<Object, InvokerHolder> invokerMap;

  public MappedInvoker(Map<Object, InvokerHolder> invokerMap) {
    this.invokerMap = invokerMap;
  }

  public void setInvokerMap(Map<Object, InvokerHolder> invokerMap) {
    this.invokerMap = invokerMap;
  }

  public void registerInvoker(String serverIdentify, InvokerHolder invokerHolder) {
    invokerMap.put(serverIdentify, invokerHolder);
  }
}
