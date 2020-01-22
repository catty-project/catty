package io.catty.core;

public interface Response {

  long getRequestId();

  void setRequestId(long requestId);

  Object getValue();

  void setValue(Object value);

  ResponseStatus getStatus();

  void setStatus(ResponseStatus status);

  boolean isError();

  enum ResponseStatus {
    OK,
    INNER_ERROR,
    OUTER_ERROR,
    UNKNOWN_ERROR,
    EXCEPTED_ERROR,
    ;
  }

}
