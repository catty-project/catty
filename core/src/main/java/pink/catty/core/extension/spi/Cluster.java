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
package pink.catty.core.extension.spi;

import java.util.List;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.extension.spi.Registry.NotifyListener;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.InvokerRegistry;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.MetaInfo;

/**
 * One Service's RPC invoker chain with cluster.
 *
 * -----------------------------------------------------------------------------------------------
 *                                                                                               |
 *                                     +-> HealthCheck -> Serialization -> Client -> NetWork     |
 *                                     +-> Consumer                                              |
 * ConsumerHandler -> ClusterInvoker ----> ...                                                   |
 *                                     +-> ...                                                   |
 *                                     +-> HealthCheck -> Serialization -> Client -> NetWork     |
 *                                                                                               |
 * -----------------------------------------------------------------------------------------------
 *
 * The "HealthCheck -> Serialization -> Client -> NetWork" invokers are called consumer.
 *
 * Cluster
 */
@SPI(scope = Scope.SINGLETON)
public interface Cluster extends InvokerRegistry<Consumer>, NotifyListener {

  void destroy();

  List<Consumer> listConsumer();

  Response onError(Consumer cluster, Consumer failedConsumer, Request request, RuntimeException e);

  /*
   * implement InvokerRegistry
   */
  @Override
  void registerInvoker(String serviceIdentify, Consumer invoker);

  @Override
  Consumer unregisterInvoker(String serviceIdentify);

  /*
   * implement NotifyListener
   */
  @Override
  default void notify(RegistryConfig registryConfig, List<MetaInfo> metaInfoCollection) {
  }
}
