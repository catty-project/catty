package io.catty.test.service;

public class Test1CheckedException extends Exception {

  public Test1CheckedException() {
  }

  public Test1CheckedException(String message) {
    super(message);
  }

  public Test1CheckedException(String message, Throwable cause) {
    super(message, cause);
  }

  public Test1CheckedException(Throwable cause) {
    super(cause);
  }

  public Test1CheckedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
