package com.nowcoder.first;

import com.nowcoder.ServerConfig;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ServerTest {

  public static void main(String[] args) {
    ServerConfig<IService> exporter = new ServerConfig<>();

    exporter.setInterfaceClazz(IService.class);
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
