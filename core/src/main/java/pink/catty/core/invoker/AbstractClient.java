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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.extension.spi.Codec;

public abstract class AbstractClient implements Client {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected static final int NEW = 0;
  protected static final int CONNECTED = 1;
  protected static final int DISCONNECTED = 2;

  private InnerClientConfig config;
  protected volatile int status = NEW;
  private Codec codec;

  private Map<Long, Response> currentTask = new ConcurrentHashMap<>();

  public AbstractClient(InnerClientConfig config, Codec codec) {
    this.config = config;
    this.codec = codec;
  }

  public Response getResponseFuture(long requestId) {
    return currentTask.remove(requestId);
  }

  public void addCurrentTask(long requestId, Response response) {
    currentTask.putIfAbsent(requestId, response);
  }

  @Override
  public Codec getCodec() {
    return codec;
  }

  @Override
  public InnerClientConfig getConfig() {
    return config;
  }

  @Override
  public boolean isAvailable() {
    return status == CONNECTED;
  }

  @Override
  public void init() {
    doOpen();
    status = CONNECTED;
  }

  @Override
  public void destroy() {
    status = DISCONNECTED;
    doClose();
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
    return Objects.equals(config, that.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(config);
  }
}
