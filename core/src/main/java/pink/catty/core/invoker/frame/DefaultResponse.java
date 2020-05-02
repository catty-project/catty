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
package pink.catty.core.invoker.frame;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DefaultResponse extends CompletableFuture<Object> implements Response {

  private long requestId;
  private Object value;

  public DefaultResponse(long requestId) {
    this.requestId = requestId;
  }

  @Override
  public long getRequestId() {
    return requestId;
  }

  @Override
  public Object getValue() {
    return value;
  }

  /**
   * The first time invoking setValue() will complete the future.
   */
  @Override
  public void setValue(Object value) {
    this.value = value;
    if (!isDone()) {
      super.complete(value);
    }
  }

  @Override
  public void await() throws InterruptedException, ExecutionException {
    get();
  }

  @Override
  public void await(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    get(timeout, unit);
  }

}
