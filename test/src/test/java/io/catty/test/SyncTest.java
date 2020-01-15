package io.catty.test;

import io.catty.Exporter;
import io.catty.Reference;
import io.catty.config.ClientConfig;
import io.catty.config.ServerConfig;
import io.catty.test.service.EchoService;
import io.catty.test.service.EchoServiceImpl;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SyncTest {

  private static final int TEST_PORT = 20550;

  private Exporter exporter;

  private Reference<EchoService> reference;

  @BeforeTest
  public void init() {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(TEST_PORT)
        .build();
    exporter = new Exporter(serverConfig);
    exporter.registerService(EchoService.class, new EchoServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:20550")
        .build();

    reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(EchoService.class);
  }

  @AfterTest
  public void destroy() {
    reference.derefer();
    exporter.unexport();
  }

  @Test
  public void returnTest() {
    String echo = UUID.randomUUID().toString();
    EchoService service = reference.refer();
    String result = service.echo(echo);
    Assert.assertEquals(echo, result);
  }

}
