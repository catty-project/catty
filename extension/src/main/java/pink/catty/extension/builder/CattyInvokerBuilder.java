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
package pink.catty.extension.builder;

import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.spi.EndpointFactory;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.Provider;
import pink.catty.core.invoker.endpoint.Client;
import pink.catty.core.invoker.endpoint.ConsumerClient;
import pink.catty.core.meta.ConsumerMeta;
import pink.catty.core.meta.ProviderMeta;
import pink.catty.invokers.consumer.ConsumerSerializationInvoker;
import pink.catty.invokers.consumer.HealthCheckInvoker;
import pink.catty.invokers.provider.ProviderInvoker;
import pink.catty.invokers.provider.ProviderSerializationInvoker;

@Extension(InvokerBuilderType.DIRECT)
public class CattyInvokerBuilder implements InvokerChainBuilder {

  @Override
  public Consumer buildConsumer(ConsumerMeta meta) {
    EndpointFactory factory = ExtensionFactory.getEndpointFactory()
        .getExtensionSingleton(meta.getEndpoint());
    Client client = factory.createClient(meta);
    Serialization serialization = ExtensionFactory
        .getSerialization()
        .getExtensionSingleton(meta.getSerialization());
    ConsumerClient consumerClient = new ConsumerClient(client, meta);
    Consumer serializationInvoker = new ConsumerSerializationInvoker(consumerClient, serialization);
    if (meta.getHealthCheckPeriod() <= 0) {
      return serializationInvoker;
    } else {
      return new HealthCheckInvoker(serializationInvoker);
    }
  }

  @Override
  public Provider buildProvider(ProviderMeta meta) {
    Serialization serialization = ExtensionFactory
        .getSerialization()
        .getExtensionSingleton(meta.getSerialization());
    ProviderInvoker providerInvoker = new ProviderInvoker(meta);
    return new ProviderSerializationInvoker(providerInvoker, serialization);
  }
}
