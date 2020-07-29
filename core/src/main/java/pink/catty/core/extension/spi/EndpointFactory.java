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

import pink.catty.core.invoker.endpoint.Client;
import pink.catty.core.invoker.endpoint.Server;
import pink.catty.core.meta.ClientMeta;
import pink.catty.core.meta.ServerMeta;

/**
 * EndpointFactory creates Client & Server and cache them. Client & Server is an endpoint to
 * communicate to remote. Different Client & Server may use different transport protocol and
 * different framework to implement remote data transport such as TCP & Netty.
 * <p>
 * You could implement your own EndpointFactory for different cache strategies or remote transport
 * protocols.
 */
@SPI(scope = Scope.SINGLETON)
public interface EndpointFactory {

  Client getClient(ClientMeta clientMeta);

  Server getServer(ServerMeta serverMeta);

}
