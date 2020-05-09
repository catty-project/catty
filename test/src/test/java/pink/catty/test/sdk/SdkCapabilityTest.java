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
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pink.catty.config.ClientConfig;
import pink.catty.config.Exporter;
import pink.catty.config.ProtocolConfig;
import pink.catty.config.Reference;
import pink.catty.config.ServerConfig;
import pink.catty.core.RpcTimeoutException;
import pink.catty.test.service.IntegrationService;
import pink.catty.test.service.IntegrationServiceImpl;
import pink.catty.test.service.exception.Test1CheckedException;
import pink.catty.test.service.exception.Test2CheckedException;

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
  public void testReturnNoUse() {
    String uuid = UUID.randomUUID().toString();
    Assert.assertEquals(uuid, integrationService.say2(uuid));
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

  @Test(expected = Test1CheckedException.class)
  public void testCheckedException() throws Exception {
    integrationService.checkedException();
  }

  @Test(expected = Test2CheckedException.class)
  public void testMultiCheckedException() throws Exception {
    integrationService.multiCheckedException();
  }

  @Test(expected = NullPointerException.class)
  public void testRuntimeException() {
    integrationService.runtimeException();
  }

  @Test(expected = Exception.class)
  public void testAsyncException0() throws Throwable {
    String msg = UUID.randomUUID().toString();
    try {
      integrationService.asyncException0(msg).get();
    } catch (ExecutionException e) {
      throw e.getCause();
    }
    Assert.fail("No exception found");
  }

  @Test(expected = Error.class)
  public void testAsyncException1() throws Throwable {
    String msg = UUID.randomUUID().toString();
    try {
      integrationService.asyncException1(msg).get();
    } catch (ExecutionException e) {
      throw e.getCause();
    }
    Assert.fail("No exception found");
  }

  @Test(expected = RuntimeException.class)
  public void testAsyncException2() throws Throwable {
    String msg = UUID.randomUUID().toString();
    try {
      integrationService.asyncException2(msg).get();
    } catch (ExecutionException e) {
      throw e.getCause();
    }
    Assert.fail("No exception found");
  }

  @Test(expected = RpcTimeoutException.class)
  public void testTimeout0() {
    integrationService.testTimeout0();
  }

  @Test
  public void testTimeout1() {
    integrationService.testTimeout1();
  }

  @Test(expected = RpcTimeoutException.class)
  public void testTimeout2() {
    integrationService.testTimeout2();
  }

}
