package pink.catty.test.spi;

import java.util.List;
import pink.catty.core.CodecException;
import pink.catty.core.extension.spi.BrokenDataPackageException;
import pink.catty.core.extension.spi.Codec;

public class Test1Codec implements Codec {

  @Override
  public byte[] encode(Object message, DataTypeEnum dataTypeEnum) throws CodecException {
    return new byte[0];
  }

  @Override
  public Object decode(byte[] data, DataTypeEnum dataTypeEnum) throws CodecException {
    return SpiTest.DEFAULT_1;
  }

  @Override
  public void readPackage(Object data, List out) throws BrokenDataPackageException {

  }
}
