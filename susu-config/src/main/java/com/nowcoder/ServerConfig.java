package com.nowcoder;

import com.nowcoder.config.RemoteConfig;
import com.nowcoder.netty.NettyServer;
import com.nowcoder.susu.SusuCodec;


/**
 * @author zrj CreateDate: 2019/9/10
 */
public class ServerConfig<T> {

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
