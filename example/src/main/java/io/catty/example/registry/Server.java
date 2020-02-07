package io.catty.example.registry;

import io.catty.config.Exporter;
import io.catty.api.RegistryConfig;
import io.catty.config.ServerConfig;
import io.catty.example.IService;
import io.catty.example.IServiceImpl;

public class Server {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();

    RegistryConfig registryConfig = new RegistryConfig();
    registryConfig.setAddress("127.0.0.1:2181");

    Exporter exporter = new Exporter(serverConfig);
    exporter.setRegistryConfig(registryConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();
  }

}
