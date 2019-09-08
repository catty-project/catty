package com.nowcoder;

import com.nowcoder.core.Exporter;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ServerTest {

  public static void main(String[] args) {
    Exporter<IService> exporter = new Exporter<>();

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
