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

import java.util.UUID;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pink.catty.test.spring.api.SpringAService;
import pink.catty.test.spring.api.SpringBService;

public class SpringTest {

  private static ApplicationContext consumer;

  @BeforeClass
  public static void init() {
    consumer = new ClassPathXmlApplicationContext("consumer_basic_test.xml");
    new ClassPathXmlApplicationContext("provider_basic_test.xml");
  }

  @Test
  public void test() {
    SpringAService aService = consumer.getBean(SpringAService.class);
    String uuid = UUID.randomUUID().toString();
    String result = aService.echo(uuid);
    Assert.assertEquals(uuid, result);

    SpringBService bService = consumer.getBean(SpringBService.class);
    uuid = UUID.randomUUID().toString();
    result = bService.echo(uuid);
    Assert.assertEquals(uuid, result);
  }

}
