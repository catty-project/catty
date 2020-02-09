package pink.catty.core.extension.spi;

import pink.catty.core.invoker.InvokerHolder;
import java.util.List;

public interface LoadBalance {

  InvokerHolder select(List<InvokerHolder> invokers);

}
