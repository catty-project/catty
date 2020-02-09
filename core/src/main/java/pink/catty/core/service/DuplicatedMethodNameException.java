package pink.catty.core.service;

public class DuplicatedMethodNameException extends RuntimeException {

  public DuplicatedMethodNameException() {
  }

  public DuplicatedMethodNameException(String message) {
    super(message);
  }

  public DuplicatedMethodNameException(String message, Throwable cause) {
    super(message, cause);
  }

  public DuplicatedMethodNameException(Throwable cause) {
    super(cause);
  }

  public DuplicatedMethodNameException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
