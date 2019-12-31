package org.fire.transport.api;

import org.fire.core.Invoker;
import org.fire.core.Request;
import org.fire.core.Response;
import org.fire.core.codec.Codec;
import org.fire.core.config.ServerConfig;
import org.fire.transport.api.worker.HashableExecutor;

public interface Server extends Invoker {

  ServerConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

  HashableExecutor getExecutor();

  @Override
  default Class getInterface() {
    return null;
  }

  @Override
  Response invoke(Request request);

  void registerInvoker(Invoker invoker);
}
