package io.catty.transport;

import io.catty.core.extension.spi.Codec;
import io.catty.core.config.ClientConfig;
import io.catty.core.Client;
import io.catty.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClient implements Client {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private static final int NEW = 0;
  private static final int CONNECTED = 1;
  private static final int DISCONNECTED = 2;

  private ClientConfig config;
  private volatile int status = NEW;
  private Codec codec;

  private Map<Long, Response> currentTask = new ConcurrentHashMap<>();

  public AbstractClient(ClientConfig config, Codec codec) {
    this.config = config;
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
  public ClientConfig getConfig() {
    return config;
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
