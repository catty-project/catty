package pink.catty.core.extension.spi;

import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;

public interface Filter {

  Response filter(Invoker invoker, Request request);

}
