package io.catty.extension.lbs;

import io.catty.core.InvokerHolder;
import io.catty.core.extension.Extension;
import io.catty.core.extension.api.LoadBalance;
import io.catty.core.meta.MetaInfo;
import io.catty.core.meta.MetaInfoEnum;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weight will be obtained from meta info of invoker. Weight should be set when it is exposing. The
 * weight scope is supposed to be 0-100 (defaulted by 100). If you do not set weight for each
 * service provider, they will be treated as the same weight 100. Weight set to 0 provider will
 * never be reached.
 */
@Extension("WEIGHTED_RANDOM")
public class WeightedRandomLoadBalance implements LoadBalance {

  @Override
  public InvokerHolder select(List<InvokerHolder> invokers) {
    int invokerSize = invokers.size();
    int totalWeight = 0;
    int[] weights = new int[invokerSize];
    for (int i = 0; i < invokerSize; i++) {
      weights[i] = getWeight(invokers.get(i));
      totalWeight += weights[i];
    }

    int offset = ThreadLocalRandom.current().nextInt(totalWeight);
    for (int i = 0; i < invokerSize; i++) {
      offset -= weights[i];
      if (offset < 0) {
        return invokers.get(i);
      }
    }
    return invokers.get(ThreadLocalRandom.current().nextInt(invokerSize));
  }

  private int getWeight(InvokerHolder invokerHolder) {
    MetaInfo metaInfo = invokerHolder.getMetaInfo();
    return metaInfo.getIntDef(MetaInfoEnum.WEIGHT, 100);
  }
}
