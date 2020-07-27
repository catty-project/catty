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
package pink.catty.example.sample;

import pink.catty.config.CattyBootstrap;
import pink.catty.example.IService;

import java.util.concurrent.TimeUnit;

public class Client {

  public static void main(String[] args) {
    IService service = CattyBootstrap.consumer(IService.class)
            .addAddress("127.0.0.1:20550")
            .getProxy();

    for (int i = 0; i < 10000; i++) {
      System.out.println(service.say0());
      sleep();
      System.out.println(service.say1("catty1"));
      sleep();
      System.out.println(service.say1("catty2"));
      sleep();
      System.out.println(service.say1("catty3"));
    }
  }

  private static void sleep() {
    try {
      TimeUnit.MILLISECONDS.sleep(500);
    } catch (Exception e) {
      // ignore
    }
  }

}
