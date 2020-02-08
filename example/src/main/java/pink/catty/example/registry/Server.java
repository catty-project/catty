package pink.catty.example.registry;

import pink.catty.config.Exporter;
import pink.catty.api.RegistryConfig;
import pink.catty.config.ServerConfig;
import pink.catty.example.IService;
import pink.catty.example.IServiceImpl;

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
