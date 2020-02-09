package pink.catty.core.invoker;

public class MethodNotFoundException extends RuntimeException {

  public MethodNotFoundException() {
  }

  public MethodNotFoundException(String message) {
    super(message);
  }

  public MethodNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public MethodNotFoundException(Throwable cause) {
    super(cause);
  }

  public MethodNotFoundException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
