package io.catty.example.pb;

import io.catty.config.Exporter;
import io.catty.config.ServerConfig;

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
