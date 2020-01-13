package io.catty;

import io.catty.api.Registry;
import io.catty.api.RegistryConfig;
import io.catty.api.Server;
import io.catty.codec.CattySerialization;
import io.catty.config.ServerConfig;
import io.catty.meta.EndpointMetaInfo;
import io.catty.meta.EndpointTypeEnum;
import io.catty.meta.MetaInfoEnum;
import io.catty.netty.NettyServer;
import io.catty.router.ServerRouterInvoker;
import io.catty.zk.ZookeeperRegistry;
import java.util.HashMap;
import java.util.Map;


public class Exporter {

  private static Map<ServerAddress, ServerRouterInvoker> serviceRouterMap = new HashMap<>();

  private static Map<ServerAddress, Server> serverMap = new HashMap<>();

  private Map<String, Invoker> serviceHandlers = new HashMap<>();

  private ServerConfig serverConfig;

  private Server server;

  private RegistryConfig registryConfig;

  private Registry registry;

  private ServerAddress address;

  public Exporter(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

  public <T> void registerService(Class<T> interfaceClass, T serviceObject) {
    serviceHandlers.put(interfaceClass.getName(),
        new ProviderInvoker<>(serviceObject, interfaceClass, new CattySerialization()));
  }

  public void export() {
    if (registry == null && registryConfig != null) {
      registry = new ZookeeperRegistry(registryConfig);
      registry.open();
    }

    address = serverConfig.getServerAddress();

    ServerRouterInvoker serverRouterInvoker;
    if (serviceRouterMap.containsKey(address)) {
      server = serverMap.get(address);
      if (server == null) {
        throw new NullPointerException("Server is null");
      }
      serverRouterInvoker = serviceRouterMap.get(address);
    } else {
      serverRouterInvoker = new ServerRouterInvoker();
      server = new NettyServer(serverConfig, serverRouterInvoker);
      serviceRouterMap.put(address, serverRouterInvoker);
      serverMap.put(address, server);
    }

    serviceHandlers.forEach((s, invoker) -> {
      serverRouterInvoker.registerInvoker(s, invoker);
      if (registry != null) {
        EndpointMetaInfo metaInfo = new EndpointMetaInfo(EndpointTypeEnum.SERVER);
        metaInfo.addMetaInfo(MetaInfoEnum.SERVER_NAME.toString(), s);
        metaInfo
            .addMetaInfo(MetaInfoEnum.ADDRESS.toString(),
                serverConfig.getServerAddress().toString());
        registry.register(metaInfo);
      }
    });

    if (!server.isOpen()) {
      server.open();
    }
  }

  public void unexport() {
    serviceRouterMap.remove(address);
    address = null;
    server.close();
    serviceHandlers.forEach((s, invoker) -> {
      EndpointMetaInfo metaInfo = new EndpointMetaInfo(EndpointTypeEnum.SERVER);
      metaInfo.addMetaInfo(MetaInfoEnum.SERVER_NAME.toString(), s);
      metaInfo
          .addMetaInfo(MetaInfoEnum.ADDRESS.toString(), serverConfig.getServerAddress().toString());
      registry.unregister(metaInfo);
    });
    registry.close();
  }
}
