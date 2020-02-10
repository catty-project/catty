package pink.catty.core.invoker;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.extension.spi.Codec;

public abstract class AbstractClient implements Client {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private static final int NEW = 0;
  private static final int CONNECTED = 1;
  private static final int DISCONNECTED = 2;

  private InnerClientConfig config;
  private volatile int status = NEW;
  private Codec codec;

  private Map<Long, Response> currentTask = new ConcurrentHashMap<>();

  public AbstractClient(InnerClientConfig config, Codec codec) {
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
  public InnerClientConfig getConfig() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractClient that = (AbstractClient) o;
    return Objects.equals(config, that.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(config);
  }
}
