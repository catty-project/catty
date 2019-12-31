package org.fire.cluster;

import org.fire.transport.api.Request;
import org.fire.transport.api.Response;

public interface Invoker<T> {

  Class<T> getInterface();

  Response invoke(Request request);

}
