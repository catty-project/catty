package io.catty.core;

import io.catty.core.extension.Codec;
import java.util.concurrent.Executor;

public interface Endpoint extends Invoker {

  void init();

  void destroy();

  boolean isAvailable();

  Codec getCodec();

  Executor getExecutor();

  Object getConfig();

}
