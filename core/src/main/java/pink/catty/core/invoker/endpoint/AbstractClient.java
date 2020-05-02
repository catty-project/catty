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
package pink.catty.core.invoker.endpoint;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ClientMeta;

public abstract class AbstractClient extends AbstractEndpoint implements Client {

  private ClientMeta clientMeta;
  private Map<Long, Response> currentTask = new ConcurrentHashMap<>();

  public AbstractClient(ClientMeta clientMeta, Codec codec) {
    super(codec);
    this.clientMeta = clientMeta;
  }

  public Response getResponseFuture(long requestId) {
    return currentTask.remove(requestId);
  }

  public void addCurrentTask(long requestId, Response response) {
    currentTask.putIfAbsent(requestId, response);
  }

  @Override
  public ClientMeta getMeta() {
    return clientMeta;
  }

  @Override
  public Executor getExecutor() {
    return null;
  }

  protected abstract void doOpen();

  protected abstract void doClose();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractClient that = (AbstractClient) o;
    return Objects.equals(clientMeta, that.clientMeta);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientMeta);
  }
}
