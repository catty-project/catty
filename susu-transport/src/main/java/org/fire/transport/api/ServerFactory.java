package org.fire.transport.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.fire.core.Invoker;
import org.fire.core.common.ServerAddress;
import org.fire.core.config.ServerConfig;
import org.fire.core.exception.TransportException;

public abstract class ServerFactory {

  private Map<ServerAddress, Server> activeServer = new ConcurrentHashMap<>();

  public Server createServer(ServerConfig serverConfig, List<Invoker> handlers) {
    ServerAddress serverAddress = serverConfig.getServerAddress();
    Server server;
    if (activeServer.containsKey(serverAddress)) {
      server = activeServer.get(serverAddress);
      if (server.isOpen()) {
        InvokerCollection invokerCollection = server.getInvokerCollection();
        if (invokerCollection == null) {
          throw new TransportException("Server's invokerCollection can't be null");
        }

        handlers.forEach(invokerCollection::registerInvoker);

        return server;
      } else {
        // This is the code path of disconnected server.
        activeServer.remove(serverAddress);
      }
    }

    server = doCreateServer(serverConfig, handlers);
    activeServer.put(serverAddress, server);
    return server;
  }

  protected abstract Server doCreateServer(ServerConfig serverConfig, List<Invoker> handler);

}
