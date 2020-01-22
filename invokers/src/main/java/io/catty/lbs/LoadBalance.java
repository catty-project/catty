package io.catty.lbs;

import io.catty.core.Invoker;
import java.util.List;

public interface LoadBalance {

  <T extends Invoker> T select(List<T> invokers);

}
