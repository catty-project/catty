package pink.catty.example.sync;

import pink.catty.config.Exporter;
import pink.catty.config.ServerConfig;
import pink.catty.core.extension.ExtensionType.SerializationType;
import pink.catty.example.IService;
import pink.catty.example.IServiceImpl;

public class Server {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.setSerializationType(SerializationType.HESSIAN2);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();
  }
}
