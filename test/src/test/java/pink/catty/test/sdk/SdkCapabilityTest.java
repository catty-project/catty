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

import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pink.catty.config.ClientConfig;
import pink.catty.config.Exporter;
import pink.catty.config.ProtocolConfig;
import pink.catty.config.Reference;
import pink.catty.config.ServerConfig;
import pink.catty.test.service.IntegrationService;
import pink.catty.test.service.IntegrationServiceImpl;

public class SdkCapabilityTest {

  private static ProtocolConfig protocolConfig;

  private static Reference<IntegrationService> reference;
  private static Exporter exporter;
  private static IntegrationService integrationService;

  @BeforeClass
  public static void init() {
    protocolConfig = ProtocolConfig.defaultConfig();
    ServerConfig serverConfig = ServerConfig.builder()
        .port(20550)
        .build();
    exporter = new Exporter(serverConfig);
    exporter.setProtocolConfig(protocolConfig);
    exporter.registerService(IntegrationService.class, new IntegrationServiceImpl());
    exporter.export();

    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setProtocolConfig(protocolConfig);
    reference.setInterfaceClass(IntegrationService.class);
    integrationService = reference.refer();
  }

  @AfterClass
  public static void destroy() {
    reference.derefer();
    exporter.unexport();
  }

  @Test
  public void testNoReturn() {
    integrationService.say0("");
  }

  @Test
  public void testReturn() {
    integrationService.say1("");
  }

  @Test
  public void testEcho() {
    String uuid = UUID.randomUUID().toString();
    Assert.assertEquals(uuid, integrationService.echo(uuid));
  }

  @Test
  public void testAsyncEcho() {
    String uuid = UUID.randomUUID().toString();
    try {
      Assert.assertEquals(uuid, integrationService.asyncEcho(uuid).get());
    } catch (Exception e) {
      Assert.fail();
    }
  }

}
