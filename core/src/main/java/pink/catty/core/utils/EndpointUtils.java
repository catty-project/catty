package pink.catty.core.utils;

import pink.catty.core.Endpoint;
import pink.catty.core.Invoker;

public abstract class EndpointUtils {

  public static void destroyInvoker(Invoker invoker) {
    if(invoker instanceof Endpoint) {
      ((Endpoint) invoker).destroy();
    }
  }

}
