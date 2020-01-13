package io.catty.cluster;

import io.catty.Invoker;
import java.util.List;

public interface LoadBalance {

  <T extends Invoker> T select(List<T> invokers);

}
