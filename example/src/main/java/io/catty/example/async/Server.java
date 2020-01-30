package io.catty.example.async;

import io.catty.config.Exporter;
import io.catty.core.config.ServerConfig;
import io.catty.example.IService;
import io.catty.example.IServiceImpl;

public class Server {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();
  }
}
