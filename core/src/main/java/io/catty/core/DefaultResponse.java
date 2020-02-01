package io.catty.core;

import io.catty.core.service.MethodMeta;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DefaultResponse extends CompletableFuture<Object> implements Response {

  private long requestId;
  private Object value;
  private MethodMeta methodMeta;

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

  @Override
  public void setValue(Object value) {
    try {
      if (!isDone()) {
        super.complete(value);
      }
      this.value = value;
    } catch (Exception e) {
      // This should not happen
      throw new CattyException(e);
    }
  }

  @Override
  public MethodMeta getMethodMeta() {
    return methodMeta;
  }

  @Override
  public void setMethodMeta(MethodMeta methodMeta) {
    this.methodMeta = methodMeta;
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
