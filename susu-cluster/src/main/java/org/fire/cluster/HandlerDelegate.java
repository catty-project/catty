package org.fire.cluster;


import org.fire.transport.api.Handler;
import org.fire.transport.api.Request;

public class HandlerDelegate implements Handler {

  private Invoker serverImpl;

  public HandlerDelegate(Invoker serverImpl) {
    this.serverImpl = serverImpl;
  }

  @Override
  public String getServiceName() {
    return serverImpl.getInterface().getName();
  }

  @Override
  public Object handle(Object message) {
    return serverImpl.invoke((Request) message);
  }
}
