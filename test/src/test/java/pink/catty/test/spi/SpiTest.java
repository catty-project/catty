package pink.catty.test.spi;

import org.junit.Assert;
import org.junit.Test;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.spi.Codec;

public class SpiTest {

  static Object DEFAULT_1 = new Object();
  static Object DEFAULT_2 = new Object();

  @Test
  public void testSpi() {
    Codec test1 = ExtensionFactory.getCodec().getExtension("test1");
    Codec test2 = ExtensionFactory.getCodec().getExtension("test2");
    Assert.assertSame(DEFAULT_1, test1.decode(null, null));
    Assert.assertSame(DEFAULT_2, test2.decode(null, null));
  }
}
