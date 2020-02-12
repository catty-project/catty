package pink.catty.core.invoker;

import pink.catty.core.config.InnerServerConfig;

public interface Server extends Endpoint, LinkedInvoker {

  @Override
  InnerServerConfig getConfig();

  InvokerRegistry getInvokerRegistry();

}
