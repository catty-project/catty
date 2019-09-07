package core;

import transport.netty.NettyServer;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class Exporter<T> {

  private T ref;

  private Class<T> interfaceClazz;

  public void setInterfaceClazz(Class<T> interfaceClazz) {
    this.interfaceClazz = interfaceClazz;
  }

  public void setRef(T ref) {
    this.ref = ref;

  }

  public void export() {
    NettyServer nettyServer = new NettyServer(new Provider<>(ref, interfaceClazz));
    nettyServer.open();
  }

}
