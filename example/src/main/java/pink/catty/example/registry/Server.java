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
package pink.catty.example.registry;

import pink.catty.config.Exporter;
import pink.catty.config.ProtocolConfig;
import pink.catty.config.ServerConfig;
import pink.catty.core.config.RegistryConfig;
import pink.catty.example.IService;
import pink.catty.example.IServiceImpl;

public class Server {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();

    ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();

    RegistryConfig registryConfig = new RegistryConfig();
    registryConfig.setAddress("127.0.0.1:2181");

    Exporter exporter = new Exporter(serverConfig);
    exporter.setRegistryConfig(registryConfig);
    exporter.setProtocolConfig(protocolConfig);
    exporter.registerService(IService.class, new IServiceImpl());
    exporter.export();
  }

}
