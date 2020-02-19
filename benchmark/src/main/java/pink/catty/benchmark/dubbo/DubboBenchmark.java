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
package pink.catty.benchmark.dubbo;

import pink.catty.benchmark.common.PojoWrkGateway;
import pink.catty.benchmark.service.PojoService;
import pink.catty.benchmark.service.PojoServiceImpl;
import java.util.HashMap;
import java.util.Map;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.remoting.Constants;

public class DubboBenchmark {

  private static ApplicationConfig application = new ApplicationConfig();
  static {
    application.setName("dubbo-benchmark");
    application.setQosEnable(false);
  }

  public static void main(String[] args) {
    startProvider();
    PojoService service = startConsumer();

    PojoWrkGateway gateway = new PojoWrkGateway();
    gateway.start(service);
  }

  private static void startProvider() {
    RegistryConfig registry = new RegistryConfig();
    ProtocolConfig protocol = new ProtocolConfig();

    registry.setAddress("N/A");

    protocol.setName("dubbo");
    protocol.setPort(25500);
    protocol.setThreads(256);
    protocol.setHost("0.0.0.0");

    ServiceConfig<PojoService> service = new ServiceConfig<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("heartbeat", "0");
    service.setParameters(attributes);
    service.setApplication(application);
    service.setRegistry(registry);
    service.setProtocol(protocol);
    service.setInterface(PojoService.class);
    service.setRef(new PojoServiceImpl());
    service.export();
  }

  private static PojoService startConsumer() {
//    RegistryConfig registry = new RegistryConfig();
//    registry.setAddress("N/A");

    ReferenceConfig<PojoService> reference = new ReferenceConfig<>();
    reference.setApplication(application);
    reference.setUrl("dubbo://localhost:25500");
    reference.setInterface(PojoService.class);
    Map<String, String> attributes = new HashMap<>();
    attributes.put("async", "false");
    attributes.put(Constants.HEARTBEAT_KEY, "0");
    attributes.put(Constants.RECONNECT_KEY, "false");
    reference.setParameters(attributes);
    return reference.get();
  }

}
