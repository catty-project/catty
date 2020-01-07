package io.catty.api;

import io.catty.Invoker;
import io.catty.codec.Codec;
import io.catty.config.ServerConfig;
import io.catty.worker.HashableExecutor;

public interface Server extends Invoker {

  ServerConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

  HashableExecutor getExecutor();
  
}
