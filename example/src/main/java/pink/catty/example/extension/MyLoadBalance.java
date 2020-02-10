package pink.catty.example.extension;

import java.util.List;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.extension.spi.LoadBalance;
import pink.catty.extension.loadbalance.RandomLoadBalance;

public class MyLoadBalance implements LoadBalance {

  private RandomLoadBalance loadBalance = new RandomLoadBalance();

  @Override
  public InvokerHolder select(List<InvokerHolder> invokers) {
    System.out.println("my sl");
    return loadBalance.select(invokers);
  }
}
