package pink.catty.example.pb;

import pink.catty.config.Exporter;
import pink.catty.config.ServerConfig;

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
