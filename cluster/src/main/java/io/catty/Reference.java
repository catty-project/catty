package io.catty;

import io.catty.api.Client;
import io.catty.api.Registry;
import io.catty.cluster.Cluster;
import io.catty.cluster.lbs.RandomLoadBalance;
import io.catty.config.ClientConfig;
import io.catty.config.RegistryConfig;
import io.catty.meta.EndpointMetaInfo;
import io.catty.meta.EndpointTypeEnum;
import io.catty.meta.MetaInfoEnum;
import io.catty.netty.NettyClient;
import io.catty.zk.ZookeeperRegistry;

public class Reference<T> {

  private Class<T> interfaceClass;

  private ClientConfig clientConfig;

  private RegistryConfig registryConfig;

  private Client client;

  private Cluster cluster;

  private Registry registry;

  private T ref;

  public Reference() {
  }

  public void setClientConfig(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

  public void setInterfaceClass(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  public T refer() {
    if (ref == null) {
      synchronized (this) {
        if (ref == null) {
          if (clientConfig == null) {
            throw new NullPointerException("ClientConfig can't be both null");
          }
          if (registryConfig == null) {
            client = new NettyClient(clientConfig);
            client.open();
            ref = new ProxyFactory<T>().getProxy(interfaceClass, client);
          } else {
            registry = new ZookeeperRegistry(registryConfig);
            cluster = new Cluster(new RandomLoadBalance());
            registry.open();
            EndpointMetaInfo metaInfo = new EndpointMetaInfo(EndpointTypeEnum.SERVER);
            metaInfo.addMetaInfo(MetaInfoEnum.SERVER_NAME.toString(), interfaceClass.getName());
            metaInfo.addMetaInfo(MetaInfoEnum.ADDRESS.toString(), clientConfig.getAddress());
            registry.subscribe(metaInfo, cluster);
            ref = new ProxyFactory<T>().getProxy(interfaceClass, cluster);
          }
        }
      }
    }
    return ref;
  }

  // fixme : registry ?
  public void derefer() {
    client.close();
  }

}
