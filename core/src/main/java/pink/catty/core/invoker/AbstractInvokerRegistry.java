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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractInvokerRegistry<T extends Invoker>
    implements InvokerRegistry<T> {

  protected Map<String, T> invokerMap = new ConcurrentHashMap<>();
  protected List<T> invokerList = new LinkedList<>();

  @Override
  public synchronized void registerInvoker(String serviceIdentify, T invoker) {
    this.invokerMap.put(serviceIdentify, invoker);
    this.invokerList.add(invoker);
  }

  @Override
  public synchronized T unregisterInvoker(String serviceIdentify) {
    T invoker = invokerMap.remove(serviceIdentify);
    this.invokerList.remove(invoker);
    return invoker;
  }

  @Override
  public T getInvoker(String invokerIdentify) {
    return invokerMap.get(invokerIdentify);
  }
}
