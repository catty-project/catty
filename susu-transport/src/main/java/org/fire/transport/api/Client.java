package org.fire.transport.api;

import org.fire.core.codec.Codec;
import org.fire.core.config.ClientConfig;

public interface Client {

  ClientConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

  Response invoke(Request request);

}
