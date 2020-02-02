package io.catty.test;

import io.catty.config.Exporter;
import io.catty.config.Reference;
import io.catty.core.config.ClientConfig;
import io.catty.core.config.ServerConfig;
import io.catty.test.service.AnnotationService;
import io.catty.test.service.AnnotationServiceImpl;
import io.catty.test.service.TestService;
import io.catty.test.service.TestServiceImpl;

public abstract class BasicTest {

  private static final int TEST_PORT = 20550;

  protected Exporter exporter;

  protected Reference<TestService> testServiceReference;

  protected Reference<AnnotationService> annotationServiceReference;

  protected void init() {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(TEST_PORT)
        .build();
    exporter = new Exporter(serverConfig);
    exporter.registerService(TestService.class, new TestServiceImpl());
    exporter.registerService(AnnotationService.class, new AnnotationServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:20550")
        .build();

    testServiceReference = new Reference<>();
    testServiceReference.setClientConfig(clientConfig);
    testServiceReference.setInterfaceClass(TestService.class);

    annotationServiceReference = new Reference<>();
    annotationServiceReference.setClientConfig(clientConfig);
    annotationServiceReference.setInterfaceClass(AnnotationService.class);
  }

  protected void destroy() {
    testServiceReference.derefer();
    annotationServiceReference.derefer();
    exporter.unexport();
  }

}
