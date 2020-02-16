package pink.catty.core.service;

public class HealthCheckException extends RuntimeException {

  private Object invoker;

  public HealthCheckException(Object invoker) {
    this.invoker = invoker;
  }

  public HealthCheckException(String message, Object invoker) {
    super(message);
    this.invoker = invoker;
  }

  public HealthCheckException(String message, Throwable cause, Object invoker) {
    super(message, cause);
    this.invoker = invoker;
  }

  public HealthCheckException(Throwable cause, Object invoker) {
    super(cause);
    this.invoker = invoker;
  }

  public HealthCheckException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace, Object invoker) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.invoker = invoker;
  }

  public HealthCheckException() {
  }

  public HealthCheckException(String message) {
    super(message);
  }

  public HealthCheckException(String message, Throwable cause) {
    super(message, cause);
  }

  public HealthCheckException(Throwable cause) {
    super(cause);
  }

  public HealthCheckException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public Object getInvoker() {
    return invoker;
  }
}
