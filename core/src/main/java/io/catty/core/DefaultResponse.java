package io.catty.core;

import io.catty.core.Response.ResponseEntity;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DefaultResponse extends CompletableFuture<ResponseEntity> implements Response {

  private long requestId;
  private ResponseEntity entity;

  public DefaultResponse(long requestId) {
    this.requestId = requestId;
  }

  @Override
  public long getRequestId() {
    return requestId;
  }

  @Override
  public Object getValue() {
    return entity.getValue();
  }

  @Override
  public ResponseStatus getStatus() {
    return entity.getStatus();
  }

  @Override
  public ResponseEntity getResponseEntity() {
    return entity;
  }

  @Override
  public void setResponseEntity(ResponseEntity entity) {
    try {
      if (!isDone()) {
        super.complete(entity);
      }
      this.entity = entity;
    } catch (Exception e) {
      // This should not happen
      throw new CattyException(e);
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

  @Override
  public boolean isError() {
    return ResponseStatus.OK != entity.getStatus();
  }
}
