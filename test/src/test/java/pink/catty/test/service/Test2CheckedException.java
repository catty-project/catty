package pink.catty.test.service;

public class Test2CheckedException extends Exception {

  public Test2CheckedException() {
  }

  public Test2CheckedException(String message) {
    super(message);
  }

  public Test2CheckedException(String message, Throwable cause) {
    super(message, cause);
  }

  public Test2CheckedException(Throwable cause) {
    super(cause);
  }

  public Test2CheckedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
