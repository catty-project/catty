package io.catty.transport;

import io.catty.codec.Codec;
import io.catty.core.Response;
import io.catty.meta.endpoint.EndpointMetaInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClient implements Endpoint {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private static final int NEW = 0;
  private static final int CONNECTED = 1;
  private static final int DISCONNECTED = 2;

  private EndpointMetaInfo metaInfo;
  private volatile int status = NEW;
  private Codec codec;

  private Map<Long, Response> currentTask = new ConcurrentHashMap<>();

  public AbstractClient(EndpointMetaInfo metaInfo, Codec codec) {
    this.metaInfo = metaInfo;
    this.codec = codec;
  }

  public Response getResponseFuture(long requestId) {
    return currentTask.remove(requestId);
  }

  public void addCurrentTask(long requestId, Response response) {
    currentTask.putIfAbsent(requestId, response);
  }

  @Override
  public Codec getCodec() {
    return codec;
  }

  @Override
  public EndpointMetaInfo getConfig() {
    return metaInfo;
  }

  @Override
  public boolean isAvailable() {
    return status == CONNECTED;
  }

  @Override
  public void init() {
    status = CONNECTED;
    doOpen();
  }

  @Override
  public void destroy() {
    doClose();
    status = DISCONNECTED;
  }

  @Override
  public Executor getExecutor() {
    return null;
  }

  protected abstract void doOpen();

  protected abstract void doClose();

}
