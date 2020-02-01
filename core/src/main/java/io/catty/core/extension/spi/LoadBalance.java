package io.catty.core.extension.spi;

import io.catty.core.InvokerHolder;
import java.util.List;

public interface LoadBalance {

  InvokerHolder select(List<InvokerHolder> invokers);

}
