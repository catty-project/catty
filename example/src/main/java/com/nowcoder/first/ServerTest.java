package com.nowcoder.first;

import com.nowcoder.RegistryConfig;
import com.nowcoder.ServerConfig;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ServerTest {

  public static void main(String[] args) {

    RegistryConfig registryConfig = new RegistryConfig();
    registryConfig.setAddress("127.0.0.1:2181");

    ServerConfig<IService> exporter = new ServerConfig<>();
    exporter.setRegistryConfig(registryConfig);
    exporter.setPort(25880);
    exporter.setInterfaceClass(IService.class);
    exporter.setRef(new IServiceImpl());
    exporter.export();
  }


  private static class IServiceImpl implements IService {

    @Override
    public String say(String name) {
      return "from rpc " + name;
    }
  }
}
