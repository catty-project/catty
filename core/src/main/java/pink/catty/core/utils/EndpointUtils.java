package pink.catty.core.utils;

import pink.catty.core.invoker.Endpoint;
import pink.catty.core.invoker.Invoker;

public abstract class EndpointUtils {

  public static void destroyInvoker(Invoker invoker) {
    if(invoker instanceof Endpoint) {
      ((Endpoint) invoker).destroy();
    }
  }

}
