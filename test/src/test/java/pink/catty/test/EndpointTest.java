package pink.catty.test;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import pink.catty.core.CodecException;
import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.EndpointFactory;
import pink.catty.core.invoker.Client;
import pink.catty.extension.factory.NettyEndpointFactory;

public class EndpointTest {

  private static final String TEST_IP = "0.0.0.0";
  private static final String TEST_IP_1 = "0.0.0.1";
  private static final int TEST_PORT = 0;
  private static final String TEST_ADDRESS = "0.0.0.0:0";
  private static final int TEST_TIMEOUT = 0;
  private static final String TEST_CODEC_TYPE = "TEST";

  @BeforeTest
  public void registerCodec() {
    ExtensionFactory.getCodec().register(TEST_CODEC_TYPE, MockCodec.class);
  }

  @Test
  public void clientInnerConfigTest() {
    InnerClientConfig clientConfig = new InnerClientConfig(TEST_IP, TEST_PORT, TEST_ADDRESS,
        TEST_TIMEOUT, TEST_CODEC_TYPE);
    Assert.assertEquals(TEST_IP, clientConfig.getServerIp());
    Assert.assertEquals(TEST_PORT, clientConfig.getServerPort());
    Assert.assertEquals(TEST_ADDRESS, clientConfig.getAddress());
    Assert.assertEquals(TEST_TIMEOUT, clientConfig.getTimeout());
    Assert.assertEquals(TEST_ADDRESS, clientConfig.getAddress());
  }

  @Test(dependsOnMethods = "clientInnerConfigTest")
  public void clientFactoryTest() {
    InnerClientConfig clientConfig = new InnerClientConfig(TEST_IP, TEST_PORT, TEST_ADDRESS,
        TEST_TIMEOUT, TEST_CODEC_TYPE);
    EndpointFactory factory = new NettyEndpointFactory();
    Client client0 = factory.createClient(clientConfig);
    Client client1 = factory.createClient(clientConfig);
    Assert.assertSame(client0, client1);
    clientConfig = new InnerClientConfig(TEST_IP_1, TEST_PORT, TEST_ADDRESS,
        TEST_TIMEOUT, TEST_CODEC_TYPE);
    Client client2 = factory.createClient(clientConfig);
    Assert.assertNotSame(client0, client2);
  }


  public static class MockCodec implements Codec {

    @Override
    public byte[] encode(Object message, DataTypeEnum dataTypeEnum) throws CodecException {
      return new byte[0];
    }

    @Override
    public Object decode(byte[] data, DataTypeEnum dataTypeEnum) throws CodecException {
      return null;
    }
  }

}
