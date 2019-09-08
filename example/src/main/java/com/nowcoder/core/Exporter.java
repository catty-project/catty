package com.nowcoder.core;

import com.nowcoder.codec.susu.SusuCodec;
import com.nowcoder.netty.NettyServer;
import com.nowcoder.remote.RemoteConfig;

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
    NettyServer nettyServer = new NettyServer(new SusuCodec(), new RemoteConfig(true),
        new Provider<>(ref, interfaceClazz));
    nettyServer.open();
  }

}
