package org.fire.transport.api;


public interface Handler {

  String getServiceName();

  Object handle(Object message);

}
