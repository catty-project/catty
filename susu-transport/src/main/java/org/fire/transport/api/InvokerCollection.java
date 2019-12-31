package org.fire.transport.api;

import org.fire.core.Invoker;

public interface InvokerCollection extends Invoker {

  void registerInvoker(Invoker invoker);

}
