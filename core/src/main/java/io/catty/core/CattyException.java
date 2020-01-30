package io.catty.core;


public class CattyException extends RuntimeException {

  public CattyException() {
  }

  public CattyException(String message) {
    super(message);
  }

  public CattyException(String message, Throwable cause) {
    super(message, cause);
  }

  public CattyException(Throwable cause) {
    super(cause);
  }

  public CattyException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
