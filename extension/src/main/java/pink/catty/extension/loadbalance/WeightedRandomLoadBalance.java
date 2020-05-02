/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.extension.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionType.LoadBalanceType;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.meta.EndpointMeta;

/**
 * Weight will be obtained from provider info of invoker. Weight should be set when it is exposing. The
 * weight scope is supposed to be 0-100 (defaulted by 100). If you do not set weight for each
 * service provider, they will be treated as the same weight 100. Weight set to 0 provider will
 * never be reached.
 */
@Extension(LoadBalanceType.WEIGHTED_RANDOM)
public class WeightedRandomLoadBalance implements LoadBalance {

  private static final String WEIGHT_KEY = "LB_WEIGHT";

  @Override
  public Invoker select(List<Invoker> invokers) {
    if(invokers.size() == 1) {
      return invokers.get(0);
    }
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

  private int getWeight(Invoker invoker) {
    EndpointMeta metaInfo = invoker.getMeta();
    return metaInfo.getIntDef(WEIGHT_KEY, 100);
  }
}
