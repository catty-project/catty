package io.catty;

import io.catty.router.ServerRouterInvoker;
import java.util.ArrayList;
import java.util.List;
import io.catty.config.ServerConfig;
import io.catty.api.Server;
import io.catty.netty.NettyServer;


public class Exporter {

  private List<Invoker> serviceHandlers = new ArrayList<>();

  private ServerConfig serverConfig;

  private Server server;

  public Exporter(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  public <T> void registerService(Class<T> interfaceClass, T serviceObject) {
    serviceHandlers.add(new ProviderInvoker<>(serviceObject, interfaceClass));
  }

  public void export() {
    ServerRouterInvoker serverRouterInvoker = new ServerRouterInvoker();
    serviceHandlers.forEach(serverRouterInvoker::registerInvoker);
    server = new NettyServer(serverConfig, serverRouterInvoker);
    server.open();
  }

  public void unexport() {
    server.close();
  }
}
