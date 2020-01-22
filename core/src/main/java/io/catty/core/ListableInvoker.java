package io.catty.core;

import java.util.List;

public abstract class ListableInvoker implements Invoker {

  protected List<Invoker> invokerList;

  public ListableInvoker(List<Invoker> invokerList) {
    this.invokerList = invokerList;
  }

  public void setInvokerList(List<Invoker> invokerList) {
    this.invokerList = invokerList;
  }
}
