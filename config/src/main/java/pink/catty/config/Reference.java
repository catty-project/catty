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
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.ServerAddress;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.CodecType;
import pink.catty.core.extension.ExtensionType.EndpointFactoryType;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.ExtensionType.LoadBalanceType;
import pink.catty.core.extension.ExtensionType.SerializationType;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.invoker.Client;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.meta.EndpointTypeEnum;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.ServiceMeta;
import pink.catty.invokers.linked.ConsumerInvoker;
import pink.catty.invokers.mapped.ClusterInvoker;
import pink.catty.extension.registry.ZookeeperRegistry;

public class Reference<T> {

  private Class<T> interfaceClass;

  private ClientConfig clientConfig;

  private RegistryConfig registryConfig;

  private Client client;

  private ClusterInvoker clusterInvoker;

  private Registry registry;

  private T ref;

  private String serializationType = SerializationType.PROTOBUF_FASTJSON.toString();

  private String loadbalanceType = LoadBalanceType.RANDOM.toString();

  private String codecType = CodecType.CATTY.toString();

  private String endpointType = EndpointFactoryType.NETTY.toString();

  public Reference() {
  }

  public void setClientConfig(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

  public void setInterfaceClass(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  public void setSerializationType(SerializationType serializationType) {
    this.serializationType = serializationType.toString();
  }

  public void setSerializationType(String serializationType) {
    this.serializationType = serializationType;
  }

  public void setLoadbalanceType(String loadbalanceType) {
    this.loadbalanceType = loadbalanceType;
  }

  public void setLoadbalanceType(LoadBalanceType loadbalanceType) {
    this.loadbalanceType = loadbalanceType.toString();
  }

  public void setCodecType(CodecType codecType) {
    this.codecType = codecType.toString();
  }

  public void setCodecType(String codecType) {
    this.codecType = codecType;
  }

  public void setEndpointType(String endpointType) {
    this.endpointType = endpointType;
  }

  public void setEndpointType(CodecType endpointType) {
    this.endpointType = endpointType.toString();
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
          metaInfo.addMetaInfo(MetaInfoEnum.SERIALIZATION, serializationType);
          metaInfo.addMetaInfo(MetaInfoEnum.CODEC, codecType);
          metaInfo.addMetaInfo(MetaInfoEnum.LOAD_BALANCE, loadbalanceType);
          metaInfo.addMetaInfo(MetaInfoEnum.ENDPOINT, endpointType);

          if (useRegistry()) {
            // todo: make Registry configurable
            registry = new ZookeeperRegistry(registryConfig);
            registry.open();
            clusterInvoker = new ClusterInvoker(metaInfo, serviceMeta);
            registry.subscribe(metaInfo, clusterInvoker);
            ref = ConsumerInvoker.getProxy(serviceMeta, clusterInvoker);
          } else {
            Map<String, InvokerHolder> invokerHolderMap = new ConcurrentHashMap<>();
            clusterInvoker = new ClusterInvoker(metaInfo, serviceMeta);
            for(ServerAddress address : clientConfig.getAddresses()) {
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
            clusterInvoker.setInvokerMap(invokerHolderMap);

            ref = ConsumerInvoker.getProxy(serviceMeta, clusterInvoker);
          }
          serviceMeta.setTarget(ref);
        }
      }
    }
    return ref;
  }

  private boolean useRegistry() {
    if(registryConfig == null) {
      return false;
    }
    if(registryConfig.getAddress().equals("N/A")) {
      return false;
    }
    return true;
  }

  public void derefer() {
    if (client != null && client.isAvailable()) {
      client.destroy();
      client = null;
    }
    if (registry != null && registry.isOpen()) {
      registry.close();
      registry = null;
    }
    if (clusterInvoker != null) {
      clusterInvoker.destroy();
    }
  }

}
