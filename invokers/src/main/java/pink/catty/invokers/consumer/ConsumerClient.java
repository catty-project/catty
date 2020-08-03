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
package pink.catty.invokers.consumer;

import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.endpoint.Client;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ConsumerMeta;

public class ConsumerClient implements Consumer {

  private volatile Client client;
  private ConsumerMeta consumerMeta;

  public ConsumerClient(Client client, ConsumerMeta consumerMeta) {
    this.client = client;
    this.consumerMeta = consumerMeta;
  }

  @Override
  public ConsumerMeta getMeta() {
    return consumerMeta;
  }

  @Override
  public Response invoke(Request request) {
    return client.invoke(request);
  }

  @Override
  public Invoker getNext() {
    return client;
  }

  @Override
  public void setNext(Invoker invoker) {
    if (invoker instanceof Client) {
      this.client = (Client) invoker;
    }
  }
}
