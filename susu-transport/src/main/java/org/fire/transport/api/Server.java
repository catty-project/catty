package org.fire.transport.api;

import org.fire.core.codec.Codec;
import org.fire.core.config.ServerConfig;
import org.fire.transport.api.worker.HashableExecutor;

public interface Server {

  ServerConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

  RoutableHandler getRoutableHandler();

  HashableExecutor getExecutor();

}
