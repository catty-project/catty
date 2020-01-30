package io.catty.core.extension;

import io.catty.core.Invoker;
import io.catty.core.meta.MetaInfo;

public interface InvokerChainBuilder {

  Invoker buildConsumerInvoker(MetaInfo metaInfo);

  Invoker buildProviderInvoker(MetaInfo metaInfo);

}
