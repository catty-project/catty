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
package pink.catty.core.extension.spi;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.invoker.AbstractInvokerRegistry;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.utils.EndpointUtils;

public abstract class AbstractCluster extends AbstractInvokerRegistry<Consumer> implements Cluster {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public synchronized void destroy() {
    invokerMap.values().forEach(EndpointUtils::destroyInvoker);
  }

  @Override
  public List<Consumer> listConsumer() {
    return Collections.unmodifiableList(invokerList);
  }

  @Override
  public void notify(RegistryConfig registryConfig, List<MetaInfo> metaInfoCollection) {

  }
}
