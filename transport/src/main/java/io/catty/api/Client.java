package io.catty.api;

import io.catty.Invoker;
import io.catty.codec.Codec;
import io.catty.config.ClientConfig;

public interface Client extends Invoker {

  ClientConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

}
