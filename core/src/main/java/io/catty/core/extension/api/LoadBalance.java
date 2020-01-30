package io.catty.core.extension.api;

import io.catty.core.InvokerHolder;
import java.util.List;

public interface LoadBalance {

  InvokerHolder select(List<InvokerHolder> invokers);

}
