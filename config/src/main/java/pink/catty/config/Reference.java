package pink.catty.config;

import pink.catty.api.Registry;
import pink.catty.api.RegistryConfig;
import pink.catty.core.invoker.Client;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.ServerAddress;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.CodecType;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.ExtensionType.LoadBalanceType;
import pink.catty.core.extension.ExtensionType.SerializationType;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.meta.EndpointTypeEnum;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.ServiceMeta;
import pink.catty.linked.ConsumerInvoker;
import pink.catty.mapped.ClusterInvoker;
import pink.catty.zk.ZookeeperRegistry;
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

          if (userRegistry()) {
            registry = new ZookeeperRegistry(registryConfig);
            registry.open();
            clusterInvoker = new ClusterInvoker(metaInfo, serviceMeta);
            registry.subscribe(metaInfo, clusterInvoker);
            ref = ConsumerInvoker.getProxy(serviceMeta, clusterInvoker);
          } else {
            List<ServerAddress> addresses = clientConfig.getAddresses();
            Map<String, InvokerHolder> invokerHolderMap = new ConcurrentHashMap<>();
            clusterInvoker = new ClusterInvoker(metaInfo, serviceMeta);
            for(ServerAddress address : addresses) {
              MetaInfo newMetaInfo = metaInfo.clone();
              newMetaInfo.addMetaInfo(MetaInfoEnum.IP, address.getIp());
              newMetaInfo.addMetaInfo(MetaInfoEnum.PORT, address.getPort());

              // todo: make InvokerChainBuilder configurable
              InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
                  .getExtensionSingleton(InvokerBuilderType.DIRECT);
              InvokerHolder invokerHolder = InvokerHolder
                  .Of(newMetaInfo, serviceMeta, chainBuilder.buildConsumerInvoker(newMetaInfo));
              invokerHolderMap.put(newMetaInfo.toString(), invokerHolder);
            }
            clusterInvoker.setInvokerMap(invokerHolderMap);

            ref = ConsumerInvoker.getProxy(serviceMeta, clusterInvoker);
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
