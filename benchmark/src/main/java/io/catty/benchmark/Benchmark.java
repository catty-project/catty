package io.catty.benchmark;

import io.catty.Exporter;
import io.catty.Reference;
import io.catty.benchmark.common.PojoWrkGateway;
import io.catty.benchmark.service.PojoService;
import io.catty.benchmark.service.PojoServiceImpl;
import io.catty.config.ClientConfig;
import io.catty.config.ServerConfig;

public class Benchmark {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .workerThreadNum(256)
        .port(25500)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.registerService(PojoService.class, new PojoServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:25500")
        .build();

    Reference<PojoService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(PojoService.class);
    PojoWrkGateway gateway = new PojoWrkGateway();
    gateway.start(reference.refer());
  }

}
