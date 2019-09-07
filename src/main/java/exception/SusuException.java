package exception;

/**
 * @author zrj CreateDate: 2019/9/6
 */
public class SusuException extends RuntimeException {

  public SusuException() {
  }

  public SusuException(String message) {
    super(message);
  }

  public SusuException(String message, Throwable cause) {
    super(message, cause);
  }

  public SusuException(Throwable cause) {
    super(cause);
  }

  public SusuException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
