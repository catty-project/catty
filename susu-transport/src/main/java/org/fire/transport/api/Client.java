package org.fire.transport.api;

import org.fire.core.Invoker;
import org.fire.core.Request;
import org.fire.core.Response;
import org.fire.core.codec.Codec;
import org.fire.core.config.ClientConfig;

public interface Client extends Invoker {

  ClientConfig getConfig();

  void open();

  boolean isOpen();

  void close();

  Codec getCodec();

  @Override
  default Class getInterface() {
    throw new UnsupportedOperationException();
  }

  @Override
  Response invoke(Request request);

}
