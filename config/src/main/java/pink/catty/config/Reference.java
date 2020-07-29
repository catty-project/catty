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
import pink.catty.core.ServerAddress;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.spi.Protocol;
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.cluster.Cluster;
import pink.catty.core.meta.ClusterMeta;
import pink.catty.core.meta.ConsumerMeta;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.service.ServiceModel;
import pink.catty.invokers.cluster.FailFastCluster;
import pink.catty.invokers.cluster.FailOverCluster;
import pink.catty.invokers.cluster.RecoveryCluster;
import pink.catty.invokers.consumer.ConsumerHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Reference<T> {

  private static final Logger logger = LoggerFactory.getLogger(Reference.class);

  private Cluster cluster;

  private Class<T> interfaceClass;

  private ClientConfig clientConfig;

  private RegistryConfig registryConfig;

  private ProtocolConfig protocolConfig;

  private Registry registry;

  private volatile T ref;

  public Reference() {
  }

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

  public T refer() {
    if (clientConfig == null) {
      throw new NullPointerException("ClientConfig can't be null");
    }
    if (ref == null) {
      synchronized (this) {
        if (ref == null) {
          ServiceModel serviceModel = ServiceModel.parse(interfaceClass);

          ClusterMeta clusterMeta = new ClusterMeta();
          clusterMeta.setServiceModel(serviceModel);
          clusterMeta.setSerialization(protocolConfig.getSerializationType());
          clusterMeta.setCodec(protocolConfig.getCodecType());
          clusterMeta.setEndpoint(protocolConfig.getEndpointType());
          clusterMeta.setHealthCheckPeriod(protocolConfig.getHeartbeatPeriod());
          clusterMeta.setLoadBalance(protocolConfig.getLoadBalanceType());
          clusterMeta.setRetryTimes(protocolConfig.getRetryTimes());
          clusterMeta.setRecoveryPeriod(protocolConfig.getRecoveryPeriod());
          String metaString = clusterMeta.toString();

          buildCluster(clusterMeta);
          Map<String, Consumer> invokerHolderMap = new ConcurrentHashMap<>();
          for (ServerAddress address : clientConfig.getAddresses()) {
            ConsumerMeta newMetaInfo = MetaInfo.parseOf(metaString, ConsumerMeta.class,
                serviceModel);
            newMetaInfo.setRemoteIp(address.getIp());
            newMetaInfo.setRemotePort(address.getPort());

            Protocol chainBuilder = ExtensionFactory.getProtocol()
                .getExtension(InvokerBuilderType.DIRECT);
            Consumer consumer = chainBuilder.buildConsumer(newMetaInfo);
            invokerHolderMap.put(newMetaInfo.toString(), consumer);
          }
          cluster.setInvokerMap(invokerHolderMap);

          ref = ConsumerHandler.getProxy(serviceModel, cluster);

          serviceModel.setTarget(ref);
        }
      }
    }
    return ref;
  }

  private void buildCluster(ClusterMeta metaInfo) {
    if (cluster == null) {
      synchronized (Reference.class) {
        if (cluster == null) {
          String clusterStrategy = protocolConfig.getClusterType();
          switch (clusterStrategy) {
            case ProtocolConfig.AUTO_RECOVERY:
              cluster = new RecoveryCluster(metaInfo);
              break;
            case ProtocolConfig.FAIL_FAST:
              cluster = new FailFastCluster(metaInfo);
              break;
            case ProtocolConfig.FAIL_OVER:
              cluster = new FailOverCluster(metaInfo);
          }
        }
      }
    }
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
    if (cluster != null) {
      cluster.destroy();
    }
    logger.info("De-refer, service: {}", interfaceClass.getName());
  }
}
