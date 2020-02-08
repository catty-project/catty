package pink.catty.core.extension.spi;

import pink.catty.core.Invoker;
import pink.catty.core.meta.MetaInfo;

public interface InvokerChainBuilder {

  Invoker buildConsumerInvoker(MetaInfo metaInfo);

  Invoker buildProviderInvoker(MetaInfo metaInfo);

}
