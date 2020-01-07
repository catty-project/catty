package io.catty.cluster;

import io.catty.Invoker;
import java.util.List;

public interface LoadBalance {

  Invoker select(List<? extends Invoker> invokers);

}
