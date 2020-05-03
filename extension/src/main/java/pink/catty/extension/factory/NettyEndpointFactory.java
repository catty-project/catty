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

import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionType.EndpointFactoryType;
import pink.catty.core.extension.spi.AbstractEndpointFactory;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.invoker.endpoint.Client;
import pink.catty.core.invoker.endpoint.Server;
import pink.catty.core.meta.ClientMeta;
import pink.catty.core.meta.ServerMeta;
import pink.catty.invokers.endpoint.NettyClient;
import pink.catty.invokers.endpoint.NettyServer;

@Extension(EndpointFactoryType.NETTY)
public class NettyEndpointFactory extends AbstractEndpointFactory {

  @Override
  protected Client doCreateClient(ClientMeta clientMeta, Codec codec) {
    return new NettyClient(clientMeta, codec);
  }

  @Override
  protected Server doCreateServer(ServerMeta serverMeta, Codec codec) {
    return new NettyServer(serverMeta, codec);
  }
}
