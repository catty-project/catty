package org.fire.transport.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.fire.core.common.ServerAddress;
import org.fire.core.config.ServerConfig;
import org.fire.core.exception.TransportException;

public abstract class ServerFactory {

  private Map<ServerAddress, Server> activeServer = new ConcurrentHashMap<>();

  public Server createServer(ServerConfig serverConfig, List<Handler> handlers) {
    ServerAddress serverAddress = serverConfig.getServerAddress();
    Server server;
    if (activeServer.containsKey(serverAddress)) {
      server = activeServer.get(serverAddress);
      if (server.isOpen()) {
        RoutableHandler routableHandler = server.getRoutableHandler();
        if (routableHandler == null) {
          throw new TransportException("Server's routableHandler can't be null");
        }

        handlers.forEach((handler) -> {
          if (handler instanceof RoutableHandler) {
            routableHandler.merge((RoutableHandler) handler);
          } else {
            routableHandler.registerHandler(handler);
          }
        });

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

  protected abstract Server doCreateServer(ServerConfig serverConfig, List<Handler> handler);

}
