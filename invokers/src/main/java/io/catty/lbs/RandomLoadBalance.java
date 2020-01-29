package io.catty.lbs;

import io.catty.core.InvokerHolder;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalance implements LoadBalance {

  @Override
  public InvokerHolder select(List<InvokerHolder> invokers) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    return invokers.get(random.nextInt(invokers.size()));
  }

}
