package io.catty.extension.lbs;

import io.catty.core.InvokerHolder;
import io.catty.core.extension.Extension;
import io.catty.core.extension.api.LoadBalance;
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
