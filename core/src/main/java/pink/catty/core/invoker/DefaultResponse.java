package pink.catty.core.invoker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DefaultResponse extends CompletableFuture<Object> implements Response {

  private long requestId;
  private Object value;

  public DefaultResponse(long requestId) {
    this.requestId = requestId;
  }

  @Override
  public long getRequestId() {
    return requestId;
  }

  @Override
  public Object getValue() {
    return value;
  }

  /**
   * The first time invoking setValue() will complete the future.
   */
  @Override
  public void setValue(Object value) {
    this.value = value;
    if (!isDone()) {
      super.complete(value);
    }
  }

  @Override
  public void await() throws InterruptedException, ExecutionException {
    get();
  }

  @Override
  public void await(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    get(timeout, unit);
  }

}
