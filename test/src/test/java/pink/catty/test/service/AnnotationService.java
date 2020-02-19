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
package pink.catty.test.service;

import pink.catty.core.service.RpcMethod;
import pink.catty.core.service.RpcService;
import java.util.concurrent.CompletableFuture;

@RpcService(name = "AnnotationTest", version = "1.0.2", group = "test", timeout = 500)
public interface AnnotationService {

  /**
   * will not timeout.
   */
  @RpcMethod(timeout = 200)
  String timeout0(String name);

  /**
   * will not timeout.
   */
  @RpcMethod(timeout = 200)
  CompletableFuture<String> asyncTimeout0(String name);

  /**
   * Is going to timeout.
   */
  @RpcMethod(timeout = 50)
  String timeout1(String name);

  /**
   * Is going to timeout.
   */
  @RpcMethod(timeout = 200)
  CompletableFuture<String> asyncTimeout1(String name);

}
