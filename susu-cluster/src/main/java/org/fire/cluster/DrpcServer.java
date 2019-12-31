package org.fire.cluster;

import java.util.concurrent.CopyOnWriteArrayList;
import org.fire.core.config.ServerConfig;
import org.fire.transport.api.Handler;
import org.fire.transport.api.Server;
import org.fire.transport.netty.NettyTransportFactory;


/**
 * The class that represents a drpc server.
 */
public class DrpcServer {

  private CopyOnWriteArrayList<Handler> serviceHandlers = new CopyOnWriteArrayList<>();

  private ServerConfig serverConfig;

  public DrpcServer(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  public <T> void registerService(Class<T> interfaceClass, T serviceObject) {
    serviceHandlers.add(new HandlerDelegate(new ServerImpl<T>(serviceObject, interfaceClass)));
  }

  public void run() {
    Server server = NettyTransportFactory.getInstance().createServer(serverConfig, serviceHandlers);
    server.open();
  }
}
