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

import java.util.HashMap;
import java.util.Map;
import pink.catty.core.ServerAddress;
import pink.catty.core.config.InnerServerConfig.InnerServerConfigBuilder;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.core.extension.ExtensionType.InvokerBuilderType;
import pink.catty.core.extension.spi.EndpointFactory;
import pink.catty.core.extension.spi.InvokerChainBuilder;
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.invoker.InvokerHolder;
import pink.catty.core.invoker.InvokerRegistry;
import pink.catty.core.invoker.Server;
import pink.catty.core.meta.EndpointTypeEnum;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.meta.MetaInfoEnum;
import pink.catty.core.service.HeartBeatSerivceImpl;
import pink.catty.core.service.HeartBeatService;
import pink.catty.core.service.ServiceMeta;


public class Exporter {

  private Map<String, InvokerHolder> serviceHandlers = new HashMap<>();

  private Server server;

  private Registry registry;

  private ServerConfig serverConfig;

  private RegistryConfig registryConfig;

  private ProtocolConfig protocolConfig;

  public Exporter(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

  public void setProtocolConfig(ProtocolConfig protocolConfig) {
    this.protocolConfig = protocolConfig;
  }

  public <T> void registerService(Class<T> interfaceClass, T serviceObject) {
    ServerAddress address = serverConfig.getServerAddress();

    ServiceMeta serviceMeta = ServiceMeta.parse(interfaceClass);
    serviceMeta.setTarget(serviceObject);

    MetaInfo metaInfo = new MetaInfo(EndpointTypeEnum.SERVER);
    metaInfo.addMetaInfo(MetaInfoEnum.PORT, address.getPort());
    metaInfo.addMetaInfo(MetaInfoEnum.IP, address.getIp());
    metaInfo.addMetaInfo(MetaInfoEnum.GROUP, serviceMeta.getGroup());
    metaInfo.addMetaInfo(MetaInfoEnum.VERSION, serviceMeta.getVersion());
    metaInfo.addMetaInfo(MetaInfoEnum.SERVICE_NAME, serviceMeta.getServiceName());
    metaInfo.addMetaInfo(MetaInfoEnum.SERIALIZATION, protocolConfig.getSerializationType());
    metaInfo.addMetaInfo(MetaInfoEnum.CODEC, protocolConfig.getCodecType());
    metaInfo.addMetaInfo(MetaInfoEnum.WORKER_NUMBER, serverConfig.getWorkerThreadNum());
    metaInfo.addMetaInfo(MetaInfoEnum.ENDPOINT, protocolConfig.getEndpointType());

    // todo: make InvokerChainBuilder configurable
    InvokerChainBuilder chainBuilder = ExtensionFactory.getInvokerBuilder()
        .getExtensionSingleton(InvokerBuilderType.DIRECT);
    InvokerHolder invokerHolder = InvokerHolder
        .Of(metaInfo, serviceMeta, chainBuilder.buildProviderInvoker(metaInfo));
    serviceHandlers.put(serviceMeta.getServiceName(), invokerHolder);
  }

  public void export() {
    if (registry == null && registryConfig != null) {
      registry = ExtensionFactory.getRegistry()
          .getExtensionSingleton(registryConfig.getRegistryType(), registryConfig);
      registry.open();
    }

    EndpointFactory factory = ExtensionFactory.getEndpointFactory()
        .getExtensionSingleton(protocolConfig.getEndpointType());
    InnerServerConfigBuilder builder = serverConfig.toInnerConfigBuilder();
    builder.codecType(protocolConfig.getCodecType());
    server = factory.createServer(builder.build());
    if (server == null) {
      // todo: more detail about creating server fail.
      throw new NullPointerException("Server is not exist");
    }
    if (!server.isAvailable()) {
      // If first open server, register heartbeat service.
      registerService(HeartBeatService.class, new HeartBeatSerivceImpl());
      server.init();
    }

    InvokerRegistry invokerRegistry = server.getInvokerRegistry();
    serviceHandlers.forEach((s, invokerHolder) -> {
      invokerRegistry.registerInvoker(s, invokerHolder);
      if (registry != null) {
        registry.register(invokerHolder.getMetaInfo());
      }
    });
  }

  public void unexport() {
    InvokerRegistry invokerRegistry = server.getInvokerRegistry();
    if (registry != null && registry.isOpen()) {
      serviceHandlers
          .forEach((s, invokerHolder) -> {
            registry.unregister(invokerHolder.getMetaInfo());
            invokerRegistry.unregisterInvoker(s);
          });
      registry.close();
    }
  }

}
