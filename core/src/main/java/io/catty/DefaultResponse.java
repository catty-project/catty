package io.catty;

import io.catty.core.Response;

public class DefaultResponse implements Response {

  private long requestId;

  private ResponseStatus status;

  private Object value;

  public DefaultResponse() {
  }

  public DefaultResponse(long requestId) {
    this.requestId = requestId;
  }

  public DefaultResponse(long requestId, ResponseStatus status, Object value) {
    this.requestId = requestId;
    this.status = status;
    this.value = value;
  }

  @Override
  public long getRequestId() {
    return requestId;
  }

  @Override
  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public ResponseStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(ResponseStatus status) {
    this.status = status;
  }

  @Override
  public boolean isError() {
    return ResponseStatus.OK != status;
  }
}
