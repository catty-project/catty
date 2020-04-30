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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pink.catty.test.service.AService;
import pink.catty.test.service.BService;

public class MultiSourceTest {

  private static AService aService;
  private static BService bService;

  private static ApplicationContext providerA;
  private static ApplicationContext providerB;

  @BeforeClass
  public static void init() {
    providerA = new ClassPathXmlApplicationContext("provider_test_a.xml");
    providerB = new ClassPathXmlApplicationContext("provider_test_b.xml");
    ApplicationContext consumer = new ClassPathXmlApplicationContext("consumer_test.xml");
    aService = consumer.getBean(AService.class);
    bService = consumer.getBean(BService.class);
  }

  @Test
  public void basicTest() {
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
