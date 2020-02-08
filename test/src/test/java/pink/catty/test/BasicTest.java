package pink.catty.test;

import pink.catty.config.Exporter;
import pink.catty.config.Reference;
import pink.catty.config.ClientConfig;
import pink.catty.config.ServerConfig;
import pink.catty.test.service.AnnotationService;
import pink.catty.test.service.AnnotationServiceImpl;
import pink.catty.test.service.TestService;
import pink.catty.test.service.TestServiceImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class BasicTest {

  private static final int TEST_PORT = 20550;

  protected Exporter exporter;

  protected Reference<TestService> testServiceReference;

  protected Reference<AnnotationService> annotationServiceReference;

  @BeforeClass
  public void init() {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(TEST_PORT)
        .build();
    exporter = new Exporter(serverConfig);
    exporter.registerService(TestService.class, new TestServiceImpl());
    exporter.registerService(AnnotationService.class, new AnnotationServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    testServiceReference = new Reference<>();
    testServiceReference.setClientConfig(clientConfig);
    testServiceReference.setInterfaceClass(TestService.class);

    annotationServiceReference = new Reference<>();
    annotationServiceReference.setClientConfig(clientConfig);
    annotationServiceReference.setInterfaceClass(AnnotationService.class);
  }

  @AfterClass
  protected void destroy() {
    testServiceReference.derefer();
    annotationServiceReference.derefer();
    exporter.unexport();
  }

}
