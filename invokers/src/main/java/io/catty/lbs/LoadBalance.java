package io.catty.lbs;

import io.catty.Invoker;
import java.util.List;

public interface LoadBalance {

  <T extends Invoker> T select(List<T> invokers);

}
