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
package pink.catty.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.ProtocolType;
import pink.catty.core.extension.spi.Protocol;
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.meta.ConsumerMeta;
import pink.catty.core.service.ServiceModel;
import pink.catty.invokers.consumer.ConsumerHandler;

/**
 * Reference is the entrance to the RPC client. After all required configs were set, invoking
 * refer() method could get a proxy instance for service interface.
 *
 * Reference instance and service interface's proxy instance are 1 on 1.
 *
 * @param <T> service type
 * @see ClientConfig
 * @see ProtocolConfig
 */
public class Reference<T> {

  private static final Logger logger = LoggerFactory.getLogger(Reference.class);

  /**
   * RPC-service's interface, refer() method will return an instance of this class.
   */
  private Class<T> interfaceClass;

  /**
   * ClientConfig defines how client works.
   */
  private ClientConfig clientConfig;

  /**
   * ProtocolConfig defines how consumer works, like how to load-balance, ha, health-check,
   */
  private ProtocolConfig protocolConfig;

  private RegistryConfig registryConfig;
  private Registry registry;
  private volatile T ref;

  public void setClientConfig(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

  public void setProtocolConfig(ProtocolConfig protocolConfig) {
    this.protocolConfig = protocolConfig;
  }

  public void setInterfaceClass(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  public Class<T> getInterfaceClass() {
    return interfaceClass;
  }

  /**
   * Create and cache the instance of 'interfaceClass'.
   *
   * @return instance of interfaceClass
   */
  public T refer() {
    if (clientConfig == null) {
      throw new NullPointerException("ClientConfig can't be null");
    }
    if (protocolConfig == null) {
      throw new NullPointerException("ProtocolConfig can't be null");
    }
    if (ref == null) {
      synchronized (this) {
        if (ref == null) {
          ServiceModel serviceModel = ServiceModel.parse(interfaceClass);

          ConsumerMeta consumerMeta = new ConsumerMeta();
          consumerMeta.setServiceModel(serviceModel);
          consumerMeta.setSerialization(protocolConfig.getSerializationType());
          consumerMeta.setCodec(protocolConfig.getCodecType());
          consumerMeta.setEndpoint(protocolConfig.getEndpointType());
          consumerMeta.setHealthCheckPeriod(protocolConfig.getHeartbeatPeriod());
          consumerMeta.setCluster(protocolConfig.getClusterType());
          consumerMeta.setLoadBalance(protocolConfig.getLoadBalanceType());
          consumerMeta.setRetryTimes(protocolConfig.getRetryTimes());
          consumerMeta.setRecoveryPeriod(protocolConfig.getRecoveryPeriod());
          consumerMeta.setDirectAddress(clientConfig.getAddresses());
          consumerMeta.setTimeout(clientConfig.getTimeout());

          Protocol protocol = ExtensionFactory.protocol().getExtension(ProtocolType.CATTY);
          Consumer consumer = protocol.buildConsumer(consumerMeta);
          ref = ConsumerHandler.getProxy(consumer);
        }
      }
    }
    return ref;
  }

  private boolean useRegistry() {
    if (registryConfig == null) {
      return false;
    }
    if (registryConfig.getAddress().equals("N/A")) {
      return false;
    }
    return true;
  }

  public void derefer() {
    if (registry != null && registry.isOpen()) {
      registry.close();
      registry = null;
    }
    logger.info("De-refer, service: {}", interfaceClass.getName());
  }
}
