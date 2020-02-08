package pink.catty.extension.lbs;

import pink.catty.core.InvokerHolder;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.LoadBalance;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple random load balance.
 */
@Extension("RANDOM")
public class RandomLoadBalance implements LoadBalance {

  @Override
  public InvokerHolder select(List<InvokerHolder> invokers) {
    if(invokers.size() == 1) {
      return invokers.get(0);
    }
    ThreadLocalRandom random = ThreadLocalRandom.current();
    return invokers.get(random.nextInt(invokers.size()));
  }

}
