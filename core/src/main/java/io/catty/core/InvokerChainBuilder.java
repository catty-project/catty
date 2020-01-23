package io.catty.core;

import io.catty.meta.MetaInfo;

public interface InvokerChainBuilder {

  Invoker buildConsumerInvoker(MetaInfo metaInfo, Client client);

  Invoker buildProviderInvoker(MetaInfo metaInfo);

}
