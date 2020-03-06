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
package pink.catty.benchmark;

import pink.catty.benchmark.common.PojoWrkGateway;
import pink.catty.benchmark.service.PojoService;
import pink.catty.benchmark.service.PojoServiceImpl;
import pink.catty.config.ClientConfig;
import pink.catty.config.Exporter;
import pink.catty.config.Reference;
import pink.catty.config.ServerConfig;
import pink.catty.core.extension.ExtensionType.SerializationType;

public class Benchmark {

  public static void main(String[] args) {
    ServerConfig serverConfig = ServerConfig.builder()
        .workerThreadNum(256)
        .port(25500)
        .build();

    Exporter exporter = new Exporter(serverConfig);
    exporter.setSerializationType(SerializationType.HESSIAN2);
    exporter.registerService(PojoService.class, new PojoServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:25500")
        .build();

    Reference<PojoService> reference = new Reference<>();
    reference.setSerializationType(SerializationType.HESSIAN2);
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(PojoService.class);
    PojoWrkGateway gateway = new PojoWrkGateway();
    gateway.start(reference.refer());
  }

}
