package io.catty.lbs;

import io.catty.core.InvokerHolder;
import java.util.List;

public interface LoadBalance {

  InvokerHolder select(List<InvokerHolder> invokers);

}
