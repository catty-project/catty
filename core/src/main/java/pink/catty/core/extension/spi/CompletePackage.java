package pink.catty.core.extension.spi;

public class CompletePackage {

  private byte[] completePackage;

  private byte[] restData;

  public CompletePackage(byte[] completePackage, byte[] restData) {
    this.completePackage = completePackage;
    this.restData = restData;
  }

  public byte[] getCompletePackage() {
    return completePackage;
  }

  public byte[] getRestData() {
    return restData;
  }

}
