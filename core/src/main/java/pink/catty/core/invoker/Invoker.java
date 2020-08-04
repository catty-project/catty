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
package pink.catty.core.invoker;

import pink.catty.core.CattyException;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.MetaInfo;

/**
 * The most important interface which represent an rpc invocation. Every struct in catty which in
 * the invoke-link is an Invoker.
 *
 * --------------------------------------------------------------------
 * | Consumer entry                           Provider entry          |
 * |       |                                       |                  |
 * |     Invoker                                 Invoker              |
 * |       |                                       |                  |
 * |      ... (Invoker Chain)                     ... (Invoker Chain) |
 * |       |                                       |                  |
 * |      Client (also an invoker)  -->   Server (also an invoker)    |
 * --------------------------------------------------------------------
 *
 *
 * A RPC invoke will be wrapped as a Request, and the return of the RPC invoke will be wrapped as a
 * Response.
 *
 * Invocation contains the whole information of the current invocation.
 */
public interface Invoker {

  /**
   * @return the meta info of this Invoker.
   */
  MetaInfo getMeta();

  /**
   *
   * @param request rpc request
   * @return rpc return
   * @throws CattyException If inner error occurred. CattyException will be
   * thrown.
   */
  Response invoke(Request request);

}
