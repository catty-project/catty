package io.catty.core.extension;

public class DuplicatedExtensionException extends RuntimeException {

  public DuplicatedExtensionException() {
  }

  public DuplicatedExtensionException(String message) {
    super(message);
  }

  public DuplicatedExtensionException(String message, Throwable cause) {
    super(message, cause);
  }

  public DuplicatedExtensionException(Throwable cause) {
    super(cause);
  }

  public DuplicatedExtensionException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
