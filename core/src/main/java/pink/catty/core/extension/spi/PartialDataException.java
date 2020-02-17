package pink.catty.core.extension.spi;

public class PartialDataException extends Exception {

  public PartialDataException() {
  }

  public PartialDataException(String message) {
    super(message);
  }

  public PartialDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public PartialDataException(Throwable cause) {
    super(cause);
  }

  public PartialDataException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
