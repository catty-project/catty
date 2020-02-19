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
package pink.catty.benchmark.motan;

import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.config.ProtocolConfig;
import com.weibo.api.motan.config.RefererConfig;
import com.weibo.api.motan.config.RegistryConfig;
import com.weibo.api.motan.config.ServiceConfig;
import com.weibo.api.motan.util.MotanSwitcherUtil;
import pink.catty.benchmark.common.PojoWrkGateway;
import pink.catty.benchmark.service.PojoService;
import pink.catty.benchmark.service.PojoServiceImpl;

public class MotanBenchmark {

  public static void main(String[] args) {
    startProvider();
    PojoService service = startConsumer();

    PojoWrkGateway gateway = new PojoWrkGateway();
    gateway.start(service);
  }

  private static void startProvider() {
    ServiceConfig<PojoService> motanDemoService = new ServiceConfig<>();

    motanDemoService.setInterface(PojoService.class);
    motanDemoService.setRef(new PojoServiceImpl());
    motanDemoService.setGroup("motan-demo-rpc");
    motanDemoService.setVersion("1.0");

    RegistryConfig registry = new RegistryConfig();
    registry.setRegProtocol("local");
    motanDemoService.setRegistry(registry);

    ProtocolConfig protocol = new ProtocolConfig();
    protocol.setId("motan");
    protocol.setName("motan");
    motanDemoService.setProtocol(protocol);
    motanDemoService.setExport("motan:25501");
    motanDemoService.export();
    MotanSwitcherUtil.setSwitcherValue(MotanConstants.REGISTRY_HEARTBEAT_SWITCHER, true);
  }

  private static PojoService startConsumer() {
    RefererConfig<PojoService> motanDemoServiceReferer = new RefererConfig<>();
    motanDemoServiceReferer.setInterface(PojoService.class);
    motanDemoServiceReferer.setGroup("motan-demo-rpc");
    motanDemoServiceReferer.setVersion("1.0");
    motanDemoServiceReferer.setRequestTimeout(3000);

    RegistryConfig registry = new RegistryConfig();
    registry.setRegProtocol("direct");
    registry.setAddress("127.0.0.1:25501");
    motanDemoServiceReferer.setRegistry(registry);

    ProtocolConfig protocol = new ProtocolConfig();
    protocol.setId("motan");
    protocol.setName("motan");
    motanDemoServiceReferer.setProtocol(protocol);

    return motanDemoServiceReferer.getRef();
  }

}
