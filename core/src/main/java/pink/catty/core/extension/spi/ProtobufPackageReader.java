package pink.catty.core.extension.spi;

public class ProtobufPackageReader implements PackageReader {

  @Override
  public CompletePackage readPackage(byte[] dataPackage)
      throws PartialDataException, BrokenDataPackageException {
    if (dataPackage == null || dataPackage.length == 0) {
      throw new BrokenDataPackageException("Data package is empty.");
    }
    int length = 0;

    int pos = 0;
    int bufferLen = dataPackage.length;
    byte tmp = dataPackage[pos++];
    if (tmp >= 0) {
      length = tmp;
    } else {
      if (pos < bufferLen) {
        int result = tmp & 127;
        if ((tmp = dataPackage[pos++]) >= 0) {
          result |= tmp << 7;
          length = result;
        } else {
          if (pos < bufferLen) {
            result |= (tmp & 127) << 7;
            if ((tmp = dataPackage[pos++]) >= 0) {
              result |= tmp << 14;
              length = result;
            } else {
              if (pos < bufferLen) {
                result |= (tmp & 127) << 14;
                if ((tmp = dataPackage[pos++]) >= 0) {
                  result |= tmp << 21;
                  length = result;
                } else {
                  if (pos < bufferLen) {
                    result |= (tmp & 127) << 21;
                    result |= (tmp = dataPackage[pos++]) << 28;
                    if (tmp < 0) {
                      throw new BrokenDataPackageException("malformed varint.");
                    }
                    length = result;
                  }
                }
              }
            }
          }
        }
      }
    }

    if (length < 0) {
      throw new BrokenDataPackageException("Negative length: " + length);
    }
    if (dataPackage.length < length) {
      throw new PartialDataException();
    } else {
      byte[] completePackage = new byte[length];
      byte[] rest = new byte[dataPackage.length - length - pos];
      System.arraycopy(dataPackage, pos, completePackage, 0, completePackage.length);
      System.arraycopy(dataPackage, length + pos, rest, 0, rest.length);
      return new CompletePackage(completePackage, rest);
    }
  }
}
