package io.catty.config;

import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.core.Client;
import io.catty.core.InvokerChainBuilder;
import io.catty.core.InvokerHolder;
import io.catty.extension.ExtensionFactory;
import io.catty.extension.ExtensionType.InvokerBuilderType;
import io.catty.extension.ExtensionType.LoadBalanceType;
import io.catty.extension.ExtensionType.SerializationType;
import io.catty.linked.ConsumerInvoker;
import io.catty.listable.Cluster;
import io.catty.meta.EndpointTypeEnum;
import io.catty.meta.MetaInfo;
import io.catty.meta.MetaInfoEnum;
import io.catty.service.ServiceMeta;
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

  private String serializationType = SerializationType.PROTOBUF_FASTJSON.toString();

  private String loadbalanceType = LoadBalanceType.RANDOM.toString();

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

  public void setSerializationType(SerializationType serializationType) {
    this.serializationType = serializationType.toString();
  }

  public void setSerializationType(String serializationType) {
    this.serializationType = serializationType;
  }

  public void setLoadbalanceType(String loadbalanceType) {
    this.loadbalanceType = loadbalanceType;
  }

  public void setLoadbalanceType(LoadBalanceType loadbalanceType) {
    this.loadbalanceType = loadbalanceType.toString();
  }

  public T refer() {
    if (ref == null) {
      synchronized (this) {
        if (ref == null) {
          if (clientConfig == null) {
            throw new NullPointerException("ClientConfig can't be both null");
          }

          ServiceMeta serviceMeta = ServiceMeta.parse(interfaceClass);

          MetaInfo metaInfo = new MetaInfo(EndpointTypeEnum.CLIENT);
          metaInfo.addMetaInfo(MetaInfoEnum.GROUP, serviceMeta.getGroup());
          metaInfo.addMetaInfo(MetaInfoEnum.VERSION, serviceMeta.getVersion());
          metaInfo.addMetaInfo(MetaInfoEnum.SERVICE_NAME, serviceMeta.getServiceName());
          metaInfo.addMetaInfo(MetaInfoEnum.SERIALIZATION, serializationType);

          // todo :
          if (registryConfig == null) {
            client = new NettyClient(clientConfig);
            client.init();

            metaInfo.addMetaInfo(MetaInfoEnum.PORT, clientConfig.getServerPort());
            metaInfo.addMetaInfo(MetaInfoEnum.IP, clientConfig.getServerIp());

            // todo: make InvokerChainBuilder configurable
            InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
                .getExtension(InvokerBuilderType.DIRECT);
            InvokerHolder invokerHolder = InvokerHolder
                .Of(metaInfo, serviceMeta, chainBuilder.buildConsumerInvoker(metaInfo, client));
            ref = ConsumerInvoker.getProxy(interfaceClass, invokerHolder);
          } else {
            registry = new ZookeeperRegistry(registryConfig);
            registry.open();

            metaInfo.addMetaInfo(MetaInfoEnum.LOAD_BALANCE, loadbalanceType);
            cluster = new Cluster(metaInfo, serviceMeta);
            registry.subscribe(metaInfo, cluster);
            ref = ConsumerInvoker
                .getProxy(interfaceClass, InvokerHolder.Of(metaInfo, serviceMeta, cluster));
          }
          serviceMeta.setTarget(ref);
        }
      }
    }
    return ref;
  }

  public void derefer() {
    if (client != null && client.isAvailable()) {
      client.destroy();
      client = null;
    }
    if (registry != null && registry.isOpen()) {
      registry.close();
      registry = null;
    }
    if (cluster != null) {
      cluster.destroy();
    }
  }

}
