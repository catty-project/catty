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
import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.config.InnerServerConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.invoker.Client;
import pink.catty.core.invoker.Server;

public abstract class AbstractEndpointFactory implements EndpointFactory {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private final Map<InnerClientConfig, Client> clientCache = new ConcurrentHashMap<>();
  private final Map<InnerServerConfig, Server> serverCache = new ConcurrentHashMap<>();

  @Override
  public Client createClient(InnerClientConfig clientConfig) {
    Client client = clientCache.get(clientConfig);
    if(client != null && !client.isAvailable()) {
      clientCache.remove(clientConfig);
      client = null;
    }
    if(client == null) {
      if(!clientCache.containsKey(clientConfig)) {
        synchronized (clientCache) {
          if(!clientCache.containsKey(clientConfig)) {
            Codec codec = ExtensionFactory.getCodec().getExtensionSingleton(clientConfig.getCodecType());
            client = doCreateClient(clientConfig, codec);
            clientCache.put(clientConfig, client);
            logger.info("EndpointFactory: a new client has bean created. ip: {}, port: {}.",
                clientConfig.getServerIp(), clientConfig.getServerPort());
          }
        }
      }
    }
    return client;
  }

  @Override
  public Server createServer(InnerServerConfig serverConfig) {
    Server server = serverCache.get(serverConfig);
    if(server != null && !server.isAvailable()) {
      serverCache.remove(serverConfig);
      server = null;
    }
    if(server == null) {
      if(!serverCache.containsKey(serverConfig)) {
        synchronized (serverCache) {
          if(!serverCache.containsKey(serverConfig)) {
            Codec codec = ExtensionFactory.getCodec().getExtensionSingleton(serverConfig.getCodecType());
            server = doCreateServer(serverConfig, codec);
            serverCache.put(serverConfig, server);
            logger.info("EndpointFactory: a new server has bean created. ip: {}, port: {}.",
                serverConfig.getServerAddress().getIp(), serverConfig.getServerAddress().getPort());
          }
        }
      }
    }
    return server;
  }

  protected abstract Client doCreateClient(InnerClientConfig clientConfig, Codec codec);

  protected abstract Server doCreateServer(InnerServerConfig serverConfig, Codec codec);

}
