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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import pink.catty.core.ServerAddress;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.invoker.Cluster;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.meta.EndpointTypeEnum;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.ServiceMeta;
import pink.catty.invokers.cluster.FailFastCluster;
import pink.catty.invokers.cluster.FailOverCluster;
import pink.catty.invokers.cluster.RecoveryCluster;
import pink.catty.invokers.linked.ConsumerInvoker;

public class Reference<T> {

  private static Cluster cluster;

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
          ServiceMeta serviceMeta = ServiceMeta.parse(interfaceClass);
          MetaInfo metaInfo = new MetaInfo(EndpointTypeEnum.CLIENT);
          metaInfo.addMetaInfo(MetaInfoEnum.GROUP, serviceMeta.getGroup());
          metaInfo.addMetaInfo(MetaInfoEnum.VERSION, serviceMeta.getVersion());
          metaInfo.addMetaInfo(MetaInfoEnum.SERVICE_NAME, serviceMeta.getServiceName());
          metaInfo.addMetaInfo(MetaInfoEnum.SERIALIZATION, protocolConfig.getSerializationType());
          metaInfo.addMetaInfo(MetaInfoEnum.CODEC, protocolConfig.getCodecType());
          metaInfo.addMetaInfo(MetaInfoEnum.LOAD_BALANCE, protocolConfig.getLoadBalanceType());
          metaInfo.addMetaInfo(MetaInfoEnum.ENDPOINT, protocolConfig.getEndpointType());
          metaInfo.addMetaInfo(MetaInfoEnum.RETRY_TIMES, protocolConfig.getRetryTimes());
          metaInfo.addMetaInfo(MetaInfoEnum.RECOVERY_PERIOD, protocolConfig.getRecoveryPeriod());
          metaInfo
              .addMetaInfo(MetaInfoEnum.HEALTH_CHECK_PERIOD, protocolConfig.getHeartbeatPeriod());

          buildCluster(metaInfo, serviceMeta);
          if (useRegistry()) {
            registry = ExtensionFactory.getRegistry()
                .getExtensionSingleton(registryConfig.getRegistryType(), registryConfig);
            registry.open();
            registry.subscribe(metaInfo, cluster);
            ref = ConsumerInvoker.getProxy(serviceMeta, cluster);
          } else {
            Map<String, InvokerHolder> invokerHolderMap = new ConcurrentHashMap<>();
            for (ServerAddress address : clientConfig.getAddresses()) {
              MetaInfo newMetaInfo = metaInfo.clone();
              newMetaInfo.addMetaInfo(MetaInfoEnum.IP, address.getIp());
              newMetaInfo.addMetaInfo(MetaInfoEnum.PORT, address.getPort());

              // todo: make InvokerChainBuilder configurable
              InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
                  .getExtensionSingleton(InvokerBuilderType.DIRECT);
              InvokerHolder invokerHolder = InvokerHolder
                  .Of(newMetaInfo, serviceMeta, chainBuilder.buildConsumerInvoker(newMetaInfo));
              invokerHolderMap.put(newMetaInfo.toString(), invokerHolder);
            }
            cluster.setInvokerMap(invokerHolderMap);

            ref = ConsumerInvoker.getProxy(serviceMeta, cluster);
          }
          serviceMeta.setTarget(ref);
        }
      }
    }
    return ref;
  }

  private void buildCluster(MetaInfo metaInfo, ServiceMeta serviceMeta) {
    if(cluster == null) {
      synchronized (Reference.class) {
        if(cluster == null) {
          String clusterStrategy = protocolConfig.getClusterType();
          switch (clusterStrategy) {
            case ProtocolConfig.AUTO_RECOVERY:
              cluster = new RecoveryCluster(metaInfo, serviceMeta);
              break;
            case ProtocolConfig.FAIL_FAST:
              cluster = new FailFastCluster(metaInfo, serviceMeta);
              break;
            case ProtocolConfig.FAIL_OVER:
              cluster = new FailOverCluster(metaInfo, serviceMeta);
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
  }

}
