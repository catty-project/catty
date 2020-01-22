package io.catty.transport;

import io.catty.codec.Codec;
import io.catty.core.Invoker;
import io.catty.meta.endpoint.EndpointMetaInfo;
import java.util.concurrent.Executor;

public interface Endpoint extends Invoker {

  EndpointMetaInfo getConfig();

  Codec getCodec();

  Executor getExecutor();

}
