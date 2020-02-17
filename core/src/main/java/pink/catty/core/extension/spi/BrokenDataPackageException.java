package pink.catty.core.extension.spi;

public class BrokenDataPackageException extends Exception {

  public BrokenDataPackageException() {
  }

  public BrokenDataPackageException(String message) {
    super(message);
  }

  public BrokenDataPackageException(String message, Throwable cause) {
    super(message, cause);
  }

  public BrokenDataPackageException(Throwable cause) {
    super(cause);
  }

  public BrokenDataPackageException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
