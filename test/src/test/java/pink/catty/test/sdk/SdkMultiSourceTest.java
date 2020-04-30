/*
 * Copyright 2020 The Catty Project
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
package pink.catty.test.sdk;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pink.catty.config.ClientConfig;
import pink.catty.config.Exporter;
import pink.catty.config.ProtocolConfig;
import pink.catty.config.Reference;
import pink.catty.config.ServerConfig;
import pink.catty.test.service.AService;
import pink.catty.test.service.AServiceImpl;
import pink.catty.test.service.BService;
import pink.catty.test.service.BServiceImpl;

public class SdkMultiSourceTest {

  private static AService aService;
  private static BService bService;

  private static Exporter exporter0;
  private static Exporter exporter1;
  private static Exporter exporter2;

  @BeforeClass
  public static void init() {
    ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();

    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();
    exporter0 = new Exporter(serverConfig);
    exporter0.setProtocolConfig(protocolConfig);
    exporter0.registerService(AService.class, new AServiceImpl());
    exporter0.registerService(BService.class, new BServiceImpl());
    exporter0.export();

    serverConfig = ServerConfig.builder()
        .port(20551)
        .build();
    exporter1 = new Exporter(serverConfig);
    exporter1.setProtocolConfig(protocolConfig);
    exporter1.registerService(AService.class, new AServiceImpl());
    exporter1.registerService(BService.class, new BServiceImpl());
    exporter1.export();

    serverConfig = ServerConfig.builder()
        .port(20552)
        .build();
    exporter2 = new Exporter(serverConfig);
    exporter2.setProtocolConfig(protocolConfig);
    exporter2.registerService(AService.class, new AServiceImpl());
    exporter2.registerService(BService.class, new BServiceImpl());
    exporter2.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .addAddress("127.0.0.1:20551")
        .addAddress("127.0.0.1:20552")
        .build();

    protocolConfig = new ProtocolConfig();
    protocolConfig.setClusterType(ProtocolConfig.AUTO_RECOVERY);
    protocolConfig.setRecoveryPeriod(3000);

    Reference<AService> aReference = new Reference<>();
    aReference.setClientConfig(clientConfig);
    aReference.setProtocolConfig(protocolConfig);
    aReference.setInterfaceClass(AService.class);
    aService = aReference.refer();

    Reference<BService> bReference = new Reference<>();
    bReference.setClientConfig(clientConfig);
    bReference.setProtocolConfig(protocolConfig);
    bReference.setInterfaceClass(BService.class);
    bService = bReference.refer();
  }

  @Test
  public void test0() {
    String a = "a";
    String b = "b";
    for(int i = 0; i < 1000; i++) {
      String a0 = aService.echo(a);
      String b0 = bService.echo(b);
      Assert.assertEquals(a0, a);
      Assert.assertEquals(b0, b);
    }
  }
}
