package io.catty.core;

public class RpcTimeoutException extends RuntimeException {

  public RpcTimeoutException() {
  }

  public RpcTimeoutException(String message) {
    super(message);
  }

  public RpcTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public RpcTimeoutException(Throwable cause) {
    super(cause);
  }

  public RpcTimeoutException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
