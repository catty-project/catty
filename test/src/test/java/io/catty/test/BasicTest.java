package io.catty.test;

import io.catty.config.Exporter;
import io.catty.config.Reference;
import io.catty.config.ClientConfig;
import io.catty.config.ServerConfig;
import io.catty.test.service.TestService;
import io.catty.test.service.TestServiceImpl;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

public abstract class BasicTest {

  private static final int TEST_PORT = 20550;

  protected Exporter exporter;

  protected Reference<TestService> reference;

  @BeforeTest
  public void init() {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(TEST_PORT)
        .build();
    exporter = new Exporter(serverConfig);
    exporter.registerService(TestService.class, new TestServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:20550")
        .build();

    reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(TestService.class);
  }

  @AfterTest
  public void destroy() {
    reference.derefer();
    exporter.unexport();
  }

}
