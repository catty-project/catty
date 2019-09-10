package com.nowcoder.lb;

import com.nowcoder.LoadBalance;
import com.nowcoder.api.remote.Invoker;
import java.util.List;
import java.util.Random;

/**
 * @author zrj CreateDate: 2019/9/10
 */
public class RandomLoadBalance implements LoadBalance {

  private Random random;

  public RandomLoadBalance() {
    this.random = new Random();
  }

  @Override
  public Invoker select(List<Invoker> invokers) {
    if(invokers.size() == 1) {
      return invokers.get(0);
    }
    return invokers.get(random.nextInt(invokers.size()));
  }
}
