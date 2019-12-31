package org.fire.transport.netty;

import java.util.List;
import org.fire.core.Invoker;
import org.fire.core.config.ServerConfig;
import org.fire.transport.api.Server;
import org.fire.transport.api.ServerFactory;

public class NettyTransportFactory extends ServerFactory {

  private NettyTransportFactory() {
  }

  private static class InstanceHolder {

    private static ServerFactory factory = new NettyTransportFactory();
  }

  public static ServerFactory getInstance() {
    return InstanceHolder.factory;
  }

  @Override
  protected Server doCreateServer(ServerConfig serverConfig, List<Invoker> handlers) {
    return new NettyServer(serverConfig, handlers);
  }
}
