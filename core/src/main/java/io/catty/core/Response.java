package io.catty.core;

import io.catty.core.Response.ResponseEntity;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Response extends CompletionStage<ResponseEntity>, Future<ResponseEntity> {

  ResponseEntity getResponseEntity();

  void setResponseEntity(ResponseEntity entity);

  long getRequestId();

  ResponseStatus getStatus();

  Object getValue();

  boolean isError();

  void await() throws InterruptedException, ExecutionException;

  void await(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException;

  /**
   * An entity holds result for Response;
   */
  class ResponseEntity {

    private ResponseStatus status;
    private Object value;

    public static ResponseEntity Of(ResponseStatus status, Object value) {
      return new ResponseEntity(status, value);
    }

    private ResponseEntity(ResponseStatus status, Object value) {
      this.status = status;
      this.value = value;
    }

    public ResponseStatus getStatus() {
      return status;
    }

    public void setStatus(ResponseStatus status) {
      this.status = status;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }
  }

  enum ResponseStatus {
    OK,
    INNER_ERROR,
    OUTER_ERROR,
    UNKNOWN_ERROR,
    EXCEPTED_ERROR,
    TIMEOUT_ERROR,
    ;
  }

}
