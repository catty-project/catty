package io.catty.core;

import io.catty.codec.Codec;
import java.util.concurrent.Executor;

public interface Endpoint extends Invoker {

  Codec getCodec();

  Executor getExecutor();

  Object getConfig();

}
