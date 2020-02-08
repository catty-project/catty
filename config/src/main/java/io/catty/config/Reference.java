package io.catty.config;

import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.core.Client;
import io.catty.core.InvokerHolder;
import io.catty.core.ServerAddress;
import io.catty.core.extension.ExtensionFactory;
import io.catty.core.extension.ExtensionType.CodecType;
import io.catty.core.extension.ExtensionType.InvokerBuilderType;
import io.catty.core.extension.ExtensionType.LoadBalanceType;
import io.catty.core.extension.ExtensionType.SerializationType;
import io.catty.core.extension.spi.InvokerChainBuilder;
import io.catty.core.meta.EndpointTypeEnum;
import io.catty.core.meta.MetaInfo;
import io.catty.core.meta.MetaInfoEnum;
import io.catty.core.service.ServiceMeta;
import io.catty.linked.ConsumerInvoker;
import io.catty.mapped.ClusterInvoker;
import io.catty.zk.ZookeeperRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Reference<T> {

  private Class<T> interfaceClass;

  private ClientConfig clientConfig;

  private RegistryConfig registryConfig;

  private Client client;

  private ClusterInvoker clusterInvoker;

  private Registry registry;

  private T ref;

  private String serializationType = SerializationType.PROTOBUF_FASTJSON.toString();

  private String loadbalanceType = LoadBalanceType.RANDOM.toString();

  private String codecType = CodecType.CATTY.toString();

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

  public void setCodecType(CodecType codecType) {
    this.codecType = codecType.toString();
  }

  public void setCodecType(String codecType) {
    this.codecType = codecType;
  }

  public T refer() {
    if (clientConfig == null) {
      throw new NullPointerException("ClientConfig can't be null");
    }
    if (ref == null) {
      synchronized (this) {
        if (ref == null) {
          ServiceMeta serviceMeta = ServiceMeta.parse(interfaceClass);
          MetaInfo metaInfo = new MetaInfo(EndpointTypeEnum.CLIENT);
          metaInfo.addMetaInfo(MetaInfoEnum.GROUP, serviceMeta.getGroup());
          metaInfo.addMetaInfo(MetaInfoEnum.VERSION, serviceMeta.getVersion());
          metaInfo.addMetaInfo(MetaInfoEnum.SERVICE_NAME, serviceMeta.getServiceName());
          metaInfo.addMetaInfo(MetaInfoEnum.SERIALIZATION, serializationType);
          metaInfo.addMetaInfo(MetaInfoEnum.CODEC, codecType);
          metaInfo.addMetaInfo(MetaInfoEnum.LOAD_BALANCE, loadbalanceType);

          // todo :
          if (userRegistry()) {
            registry = new ZookeeperRegistry(registryConfig);
            registry.open();
            clusterInvoker = new ClusterInvoker(metaInfo, serviceMeta);
            registry.subscribe(metaInfo, clusterInvoker);
            ref = ConsumerInvoker
                .getProxy(interfaceClass, serviceMeta, clusterInvoker);
          } else {
            List<ServerAddress> addresses = clientConfig.getAddresses();
            Map<String, InvokerHolder> invokerHolderMap = new ConcurrentHashMap<>();
            clusterInvoker = new ClusterInvoker(metaInfo, serviceMeta);
            for(ServerAddress address : addresses) {
              metaInfo.addMetaInfo(MetaInfoEnum.IP, address.getIp());
              metaInfo.addMetaInfo(MetaInfoEnum.PORT, address.getPort());

              // todo: make InvokerChainBuilder configurable
              InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
                  .getExtensionSingleton(InvokerBuilderType.DIRECT);
              InvokerHolder invokerHolder = InvokerHolder
                  .Of(metaInfo, serviceMeta, chainBuilder.buildConsumerInvoker(metaInfo));
              invokerHolderMap.put(metaInfo.toString(), invokerHolder);
            }
            clusterInvoker.setInvokerMap(invokerHolderMap);

            ref = ConsumerInvoker.getProxy(interfaceClass, serviceMeta, clusterInvoker);
          }
          serviceMeta.setTarget(ref);
        }
      }
    }
    return ref;
  }

  private boolean userRegistry() {
    if(registryConfig == null) {
      return false;
    }
    if(registryConfig.getAddress().equals("N/A")) {
      return false;
    }
    return true;
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
    if (clusterInvoker != null) {
      clusterInvoker.destroy();
    }
  }

}
