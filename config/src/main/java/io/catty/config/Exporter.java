package io.catty.config;

import io.catty.ServerAddress;
import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.core.InvokerChainBuilder;
import io.catty.core.InvokerHolder;
import io.catty.core.Server;
import io.catty.extension.ExtensionFactory;
import io.catty.extension.ExtensionType.InvokerBuilderType;
import io.catty.extension.ExtensionType.SerializationType;
import io.catty.mapped.ServerRouterInvoker;
import io.catty.meta.EndpointTypeEnum;
import io.catty.meta.MetaInfo;
import io.catty.meta.MetaInfoEnum;
import io.catty.service.ServiceMeta;
import io.catty.transport.netty.NettyServer;
import io.catty.zk.ZookeeperRegistry;
import java.util.HashMap;
import java.util.Map;


public class Exporter {

  private static Map<ServerAddress, ServerRouterInvoker> serviceRouterMap = new HashMap<>();

  private static Map<ServerAddress, Server> serverMap = new HashMap<>();

  private Map<String, InvokerHolder> serviceHandlers = new HashMap<>();

  private ServerConfig serverConfig;

  private Server server;

  private RegistryConfig registryConfig;

  private Registry registry;

  private ServerAddress address;

  private String serializationType = SerializationType.PROTOBUF_FASTJSON.toString();

  public Exporter(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    this.address = serverConfig.getServerAddress();
  }

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

  public void setSerializationType(SerializationType serializationType) {
    this.serializationType = serializationType.toString();
  }

  public void setSerializationType(String serializationType) {
    this.serializationType = serializationType;
  }

  public <T> void registerService(Class<T> interfaceClass, T serviceObject) {
    ServiceMeta serviceMeta = ServiceMeta.parse(interfaceClass);
    serviceMeta.setTarget(serviceObject);

    MetaInfo metaInfo = new MetaInfo(EndpointTypeEnum.SERVER);
    metaInfo.addMetaInfo(MetaInfoEnum.PORT, address.getPort());
    metaInfo.addMetaInfo(MetaInfoEnum.IP, address.getIp());
    metaInfo.addMetaInfo(MetaInfoEnum.GROUP, serviceMeta.getGroup());
    metaInfo.addMetaInfo(MetaInfoEnum.VERSION, serviceMeta.getVersion());
    metaInfo.addMetaInfo(MetaInfoEnum.SERVICE_NAME, serviceMeta.getServiceName());
    metaInfo.addMetaInfo(MetaInfoEnum.SERIALIZATION, serializationType);
    metaInfo.addMetaInfo(MetaInfoEnum.WORKER_NUMBER, serverConfig.getWorkerThreadNum());

    // todo: make InvokerChainBuilder configurable
    InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
        .getExtension(InvokerBuilderType.DIRECT);
    InvokerHolder invokerHolder = InvokerHolder
        .Of(metaInfo, serviceMeta, chainBuilder.buildProviderInvoker(metaInfo));
    serviceHandlers.put(interfaceClass.getName(), invokerHolder);
  }

  public void export() {
    if (registry == null && registryConfig != null) {
      registry = new ZookeeperRegistry(registryConfig);
      registry.open();
    }

    ServerRouterInvoker serverRouterInvoker;
    if (serviceRouterMap.containsKey(address)) {
      server = serverMap.get(address);
      if (server == null) {
        throw new NullPointerException("Server is not exist");
      }
      serverRouterInvoker = serviceRouterMap.get(address);
    } else {
      serverRouterInvoker = new ServerRouterInvoker();
      server = new NettyServer(serverConfig, serverRouterInvoker);
      serviceRouterMap.put(address, serverRouterInvoker);
      serverMap.put(address, server);
    }

    serviceHandlers.forEach((s, invokerHolder) -> {
      serverRouterInvoker.registerInvoker(s, invokerHolder);
      if (registry != null) {
        registry.register(invokerHolder.getMetaInfo());
      }
    });

    if (!server.isAvailable()) {
      server.init();
    }
  }

  public void unexport() {
    serviceRouterMap.remove(address);
    address = null;
    server.destroy();
    if (registry != null && registry.isOpen()) {
      serviceHandlers
          .forEach((s, invokerHolder) -> registry.unregister(invokerHolder.getMetaInfo()));
      registry.close();
    }
  }

}
