package pink.catty.core.extension.spi;

public interface PackageReader {

  /**
   * Reading data package received from tcp or other stream completely.
   *
   * @param dataPackage data received from tcp.
   * @return return an complete data.
   * @throws BrokenDataPackageException If reading data package occurs some error.
   * @throws PartialDataException If dataPackage does not contain an complete data.
   */
  CompletePackage readPackage(byte[] dataPackage) throws PartialDataException, BrokenDataPackageException;

}
