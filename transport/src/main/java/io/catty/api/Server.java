package io.catty.api;

import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;
import io.catty.api.worker.HashableExecutor;
import io.catty.codec.Codec;
import io.catty.config.ServerConfig;

public interface Server extends Invoker {

  ServerConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

  HashableExecutor getExecutor();

  @Override
  default Class getInterface() {
    return getInvoker().getInterface();
  }

  @Override
  default Response invoke(Request request) {
    return getInvoker().invoke(request);
  }

  Invoker getInvoker();
}
