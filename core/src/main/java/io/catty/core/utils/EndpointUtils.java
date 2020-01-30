package io.catty.core.utils;

import io.catty.core.Endpoint;
import io.catty.core.Invoker;

public abstract class EndpointUtils {

  public static void destroyInvoker(Invoker invoker) {
    if(invoker instanceof Endpoint) {
      ((Endpoint) invoker).destroy();
    }
  }

}
