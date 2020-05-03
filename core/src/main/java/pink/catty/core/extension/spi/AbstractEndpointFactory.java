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
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.invoker.endpoint.Client;
import pink.catty.core.invoker.endpoint.Server;
import pink.catty.core.meta.ClientMeta;
import pink.catty.core.meta.ServerMeta;

public abstract class AbstractEndpointFactory implements EndpointFactory {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private static final Map<ClientMeta, Client> clientCache = new ConcurrentHashMap<>();
  private static final Map<ServerMeta, Server> serverCache = new ConcurrentHashMap<>();

  @Override
  public Client createClient(ClientMeta clientMeta) {
    Client client = clientCache.get(clientMeta);
    if(client != null && client.isClosed()) {
      clientCache.remove(clientMeta);
      client = null;
    }
    if (client == null) {
      synchronized (clientCache) {
        if (!clientCache.containsKey(clientMeta)) {
          Codec codec = ExtensionFactory.getCodec()
              .getExtensionSingleton(clientMeta.getCodec());
          client = doCreateClient(clientMeta, codec);
          client.open();
          clientCache.put(clientMeta, client);
          logger.info("EndpointFactory: a new client has bean created. ip: {}, port: {}.",
              clientMeta.getRemoteIp(), clientMeta.getRemotePort());
        }
      }
    }
    return client;
  }

  @Override
  public Server createServer(ServerMeta serverMeta) {
    Server server = serverCache.get(serverMeta);
    if(server != null && server.isClosed()) {
      serverCache.remove(serverMeta);
      server = null;
    }
    if (server == null) {
      synchronized (serverCache) {
        if (!serverCache.containsKey(serverMeta)) {
          Codec codec = ExtensionFactory.getCodec()
              .getExtensionSingleton(serverMeta.getCodec());
          server = doCreateServer(serverMeta, codec);
          server.open();
          serverCache.put(serverMeta, server);
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
