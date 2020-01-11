package io.catty.benchmark;

import io.catty.Exporter;
import io.catty.Reference;
import io.catty.benchmark.common.ProtobufWrkGateway;
import io.catty.benchmark.service.ProtobufService;
import io.catty.benchmark.service.ProtobufServiceImpl;
import io.catty.config.ClientConfig;
import io.catty.config.ServerConfig;

public class Benchmark {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .workerThreadNum(256)
        .port(25500)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.registerService(ProtobufService.class, new ProtobufServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:25500")
        .build();

    Reference<ProtobufService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(ProtobufService.class);
    ProtobufWrkGateway gateway = new ProtobufWrkGateway();
    gateway.start(reference.refer());
  }

}
