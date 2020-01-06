package io.catty;

import io.catty.config.ClientConfig;
import io.catty.api.Client;
import io.catty.netty.NettyClient;

public class Reference<T> {

  private Class<T> interfaceClass;

  private ClientConfig clientConfig;

  private Client client;

  public Reference() {

  }

  public void setClientConfig(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  public void setInterfaceClass(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  public T refer() {
    client = new NettyClient(clientConfig);
    client.open();
    return new ProxyFactory<T>().getProxy(interfaceClass, client);
  }

  public void derefer() {
    client.close();
  }

}
