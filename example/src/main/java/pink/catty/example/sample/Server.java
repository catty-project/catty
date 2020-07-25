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
import pink.catty.example.IServiceImpl;

public class Server {

  public static void main(String[] args) {
    CattyBootstrap.provider()
            .port(20550)
            .getExporter()
            .registerService(IService.class, new IServiceImpl())
            .export();
  }
}
