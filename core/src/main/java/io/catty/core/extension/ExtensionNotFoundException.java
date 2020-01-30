package io.catty.core.extension;

public class ExtensionNotFoundException extends RuntimeException {

  public ExtensionNotFoundException() {
  }

  public ExtensionNotFoundException(String message) {
    super(message);
  }

  public ExtensionNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExtensionNotFoundException(Throwable cause) {
    super(cause);
  }

  public ExtensionNotFoundException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
