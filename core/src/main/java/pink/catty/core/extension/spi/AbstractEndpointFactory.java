/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.core.extension.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.ServerAddress;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.invoker.endpoint.Client;
import pink.catty.core.invoker.endpoint.Server;
import pink.catty.core.meta.ClientMeta;
import pink.catty.core.meta.ServerMeta;

public abstract class AbstractEndpointFactory implements EndpointFactory {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private static final Map<ServerAddress, Client> clientCache = new ConcurrentHashMap<>();
  private static final Map<Integer, Server> serverCache = new ConcurrentHashMap<>();

  @Override
  public Client getClient(ClientMeta clientMeta) {
    ServerAddress address = new ServerAddress(clientMeta.getRemoteIp(), clientMeta.getRemotePort());
    Client client = clientCache.get(address);
    if(client != null && client.isClosed()) {
      clientCache.remove(address);
      client = null;
    }
    if (client == null) {
      synchronized (clientCache) {
        if (!clientCache.containsKey(address)) {
          Codec codec = ExtensionFactory.getCodec().getExtension(clientMeta.getCodec());
          client = doCreateClient(clientMeta, codec);
          client.open();
          clientCache.put(address, client);
          logger.info("EndpointFactory: a new client has bean created. ip: {}, port: {}.",
              clientMeta.getRemoteIp(), clientMeta.getRemotePort());
        }
      }
    }
    return client;
  }

  @Override
  public Server getServer(ServerMeta serverMeta) {
    int port = serverMeta.getLocalPort();
    Server server = serverCache.get(port);
    if(server != null && server.isClosed()) {
      serverCache.remove(port);
      server = null;
    }
    if (server == null) {
      synchronized (serverCache) {
        if (!serverCache.containsKey(port)) {
          Codec codec = ExtensionFactory.getCodec().getExtension(serverMeta.getCodec());
          server = doCreateServer(serverMeta, codec);
          server.open();
          serverCache.put(port, server);
          logger.info("EndpointFactory: a new server has bean created. ip: {}, port: {}.",
              serverMeta.getLocalIp(), serverMeta.getLocalPort());
        }
      }
    }
    return server;
  }

  protected abstract Client doCreateClient(ClientMeta clientMeta, Codec codec);

  protected abstract Server doCreateServer(ServerMeta serverMeta, Codec codec);

}
