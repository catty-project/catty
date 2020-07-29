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
package pink.catty.core.invoker.endpoint;

import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.EndpointIllegalStateException;
import pink.catty.core.extension.spi.Codec;

public abstract class AbstractEndpoint implements Endpoint {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected static final int NEW = 0;
  protected static final int CONNECTED = 1;
  protected static final int DISCONNECTED = 2;

  private final Codec codec;
  private AtomicInteger status;

  public AbstractEndpoint(Codec codec) {
    this.status = new AtomicInteger(NEW);
    this.codec = codec;
  }

  @Override
  public Codec getCodec() {
    return codec;
  }

  @Override
  public boolean isAvailable() {
    return status.get() == CONNECTED;
  }

  @Override
  public boolean isClosed() {
    return status.get() == DISCONNECTED;
  }

  @Override
  public void open() {
    if (status.compareAndSet(NEW, CONNECTED)) {
      doOpen();
      logger.info("Opened an endpoint, {}", getMeta().toString());
    } else {
      throw new EndpointIllegalStateException(
          "Endpoint's status is illegal, status: " + status + " config: " + getMeta()
              .toString());
    }
  }

  @Override
  public void close() {
    if (status.get() == DISCONNECTED) {
      return;
    }
    if (status.compareAndSet(CONNECTED, DISCONNECTED)) {
      doClose();
      logger.info("Closed an endpoint, {}", getMeta().toString());
    } else {
      throw new EndpointIllegalStateException(
          "Endpoint's status is illegal, status: " + status + " config: " + getMeta()
              .toString());
    }
  }

  protected abstract void doOpen();

  protected abstract void doClose();
}
