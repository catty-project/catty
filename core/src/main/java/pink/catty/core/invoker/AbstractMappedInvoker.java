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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMappedInvoker implements Invoker, MappedInvoker {

  protected Map<String, InvokerHolder> invokerMap;

  public AbstractMappedInvoker() {
    this(new ConcurrentHashMap<>());
  }

  public AbstractMappedInvoker(Map<String, InvokerHolder> invokerMap) {
    this.invokerMap = invokerMap;
  }

  @Override
  public void setInvokerMap(Map<String, InvokerHolder> invokerMap) {
    this.invokerMap = invokerMap;
  }

  @Override
  public void registerInvoker(String serviceIdentify, InvokerHolder invokerHolder) {
    invokerMap.put(serviceIdentify, invokerHolder);
  }

  @Override
  public InvokerHolder unregisterInvoker(String serviceIdentify) {
    return invokerMap.remove(serviceIdentify);
  }

  @Override
  public InvokerHolder getInvoker(String invokerIdentify) {
    return invokerMap.get(invokerIdentify);
  }


}
