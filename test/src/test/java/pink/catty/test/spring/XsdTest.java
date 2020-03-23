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
package pink.catty.test.spring;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pink.catty.config.ClientConfig;
import pink.catty.config.ProtocolConfig;
import pink.catty.config.ServerConfig;
import pink.catty.core.ServerAddress;

public class XsdTest {

  @Test
  public void testProtocol() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("xsd_test.xml");
    ProtocolConfig protocolConfig = context.getBean(ProtocolConfig.class);
    Assert.assertEquals(protocolConfig.getClusterType(), "auto-recovery");
    Assert.assertEquals(protocolConfig.getCodecType(), "catty");
    Assert.assertEquals(protocolConfig.getSerializationType(), "hessian2");
    Assert.assertEquals(protocolConfig.getEndpointType(), "netty");
    Assert.assertEquals(protocolConfig.getLoadBalanceType(), "random");
  }

  @Test
  public void testClientConfig() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("xsd_test.xml");
    ClientConfig clientConfig = context.getBean(ClientConfig.class);
    Assert.assertEquals(clientConfig.getTimeout(), 3000);
    List<ServerAddress> addressList = clientConfig.getAddresses();
    Assert.assertEquals(addressList.size(), 2);
    Assert.assertEquals(addressList.get(0), new ServerAddress("127.0.0.1", 8080));
    Assert.assertEquals(addressList.get(1), new ServerAddress("127.0.0.1", 8081));
  }

  @Test
  public void testServerConfig() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("xsd_test.xml");
    ServerConfig serverConfig = context.getBean(ServerConfig.class);
    Assert.assertEquals(serverConfig.getPort(), 25001);
    Assert.assertEquals(serverConfig.getWorkerThreadNum(), 300);
  }

}
