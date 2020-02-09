package pink.catty.example.multi_server;

import pink.catty.config.Exporter;
import pink.catty.config.ServerConfig;
import pink.catty.example.IService;
import pink.catty.example.IServiceImpl;

public class Server {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();

    serverConfig = ServerConfig.builder()
        .port(20551)
        .build();

    exporter = new Exporter(serverConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();

    serverConfig = ServerConfig.builder()
        .port(20552)
        .build();

    exporter = new Exporter(serverConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();
  }
}
