package pink.catty.core.invoker;

import pink.catty.core.extension.spi.Codec;
import java.util.concurrent.Executor;

public interface Endpoint extends Invoker {

  void init();

  void destroy();

  boolean isAvailable();

  Codec getCodec();

  Executor getExecutor();

  Object getConfig();

}
