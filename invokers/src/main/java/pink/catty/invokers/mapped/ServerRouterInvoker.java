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
package pink.catty.invokers.mapped;

import pink.catty.core.CattyException;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.invoker.AbstractMappedInvoker;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.service.MethodMeta;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRouterInvoker extends AbstractMappedInvoker {

  public ServerRouterInvoker() {
    super(new ConcurrentHashMap<>());
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    String serviceName = request.getInterfaceName();
    InvokerHolder invokerHolder = invokerMap.get(serviceName);
    if (invokerHolder == null) {
      throw new CattyException(
          "No such service found! RpcService name: " + request.getInterfaceName());
    }

    if (invocation == null) {
      invocation = new Invocation(InvokerLinkTypeEnum.PROVIDER);
    }
    invocation.setTarget(invokerHolder.getServiceMeta().getTarget());
    MethodMeta methodMeta = invokerHolder.getServiceMeta()
        .getMethodMetaByName(request.getMethodName());
    invocation.setInvokedMethod(methodMeta);
    invocation.setServiceMeta(invokerHolder.getServiceMeta());
    invocation.setMetaInfo(invokerHolder.getMetaInfo());
    return invokerHolder.getInvoker().invoke(request, invocation);
  }

}
