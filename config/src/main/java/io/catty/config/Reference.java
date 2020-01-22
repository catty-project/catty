package io.catty.config;

import io.catty.ConsumerInvocationHandler;
import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.listable.Cluster;
import io.catty.linked.SerializationInvoker;
import io.catty.lbs.RandomLoadBalance;
import io.catty.meta.endpoint.EndpointMetaInfo;
import io.catty.meta.endpoint.EndpointTypeEnum;
import io.catty.meta.endpoint.MetaInfoEnum;
import io.catty.transport.netty.NettyClient;
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
            client.init();
            InvokerChainBuilder chainBuilder = new InvokerChainBuilder();
            chainBuilder.setSourceInvoker(client);
            chainBuilder.registerInterceptor(new SerializationInvoker());
            ref = ConsumerInvocationHandler.getProxy(interfaceClass, chainBuilder.buildInvoker());
          } else {
            registry = new ZookeeperRegistry(registryConfig);
            registry.open();
            cluster = new Cluster(new RandomLoadBalance());
            EndpointMetaInfo metaInfo = new EndpointMetaInfo(EndpointTypeEnum.SERVER);
            metaInfo.addMetaInfo(MetaInfoEnum.SERVER_NAME, interfaceClass.getName());
            metaInfo.addMetaInfo(MetaInfoEnum.ADDRESS, clientConfig.getAddress());
            registry.subscribe(metaInfo, cluster);
            ref = ConsumerInvocationHandler.getProxy(interfaceClass, cluster);
          }
        }
      }
    }
    return ref;
  }

  public void derefer() {
    if(client != null && client.isAvailable()) {
      client.destroy();
      client = null;
    }
    if(registry != null && registry.isOpen()) {
      registry.close();
      registry = null;
    }
    if(cluster != null) {
      cluster.destroy();
    }
  }

}
