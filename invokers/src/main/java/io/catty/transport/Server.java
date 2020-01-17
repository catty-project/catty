package io.catty.transport;

import io.catty.Invoker;
import io.catty.codec.Codec;
import io.catty.config.ServerConfig;
import io.catty.transport.worker.HashableExecutor;

public interface Server extends Invoker {

  ServerConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

  HashableExecutor getExecutor();
  
}
