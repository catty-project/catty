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
package pink.catty.extension.factory;

import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.config.InnerServerConfig;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.AbstractEndpointFactory;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.invoker.Client;
import pink.catty.core.invoker.Server;
import pink.catty.invokers.endpoint.NettyClient;
import pink.catty.invokers.endpoint.NettyServer;
import pink.catty.invokers.mapped.ServerRouterInvoker;

@Extension("NETTY")
public class NettyEndpointFactory extends AbstractEndpointFactory {

  @Override
  protected Client doCreateClient(InnerClientConfig clientConfig, Codec codec) {
    return new NettyClient(clientConfig, codec);
  }

  @Override
  protected Server doCreateServer(InnerServerConfig serverConfig, Codec codec) {
    return new NettyServer(serverConfig, codec, new ServerRouterInvoker());
  }
}
