package org.fire.cluster;

import java.util.concurrent.CopyOnWriteArrayList;
import org.fire.cluster.router.InvokerRouter;
import org.fire.core.Invoker;
import org.fire.core.config.ServerConfig;
import org.fire.transport.api.Server;
import org.fire.transport.netty.NettyServer;


public class Exporter {

  private CopyOnWriteArrayList<Invoker> serviceHandlers = new CopyOnWriteArrayList<>();

  private ServerConfig serverConfig;

  private Server server;

  public Exporter(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  public <T> void registerService(Class<T> interfaceClass, T serviceObject) {
    serviceHandlers.add(new Provider<T>(serviceObject, interfaceClass));
  }

  public void export() {
    InvokerRouter invokerRouter = new InvokerRouter();
    serviceHandlers.forEach(invokerRouter::registerInvoker);
    server = new NettyServer(serverConfig, invokerRouter);
    server.open();
  }

  public void unexport() {
    server.close();
  }
}
