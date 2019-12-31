package org.fire.example.pb;

import org.fire.cluster.Exporter;
import org.fire.core.config.ServerConfig;

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
