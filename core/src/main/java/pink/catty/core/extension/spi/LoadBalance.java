package pink.catty.core.extension.spi;

import pink.catty.core.InvokerHolder;
import java.util.List;

public interface LoadBalance {

  InvokerHolder select(List<InvokerHolder> invokers);

}
