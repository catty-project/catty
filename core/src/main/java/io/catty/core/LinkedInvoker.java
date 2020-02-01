package io.catty.core;

abstract public class LinkedInvoker implements Invoker {

  protected Invoker next;

  public LinkedInvoker() {
  }

  public LinkedInvoker(Invoker next) {
    this.next = next;
  }

  public void setNext(Invoker next) {
    this.next = next;
  }

}
