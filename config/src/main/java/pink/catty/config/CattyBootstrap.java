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

/**
 * Catty Bootstrap API. If you use spring to config catty, this class may be useless.
 * <p>
 * Facade for protocol & provider or consumer, export a stable api.
 */
public class CattyBootstrap {

    public static class ConsumerBootstrap<T> {

        private ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        private ClientConfig.ClientConfigBuilder clientConfigBuilder = ClientConfig.builder();
        private Class<T> interfaceClass;
        private ClientConfig clientConfig;
        private Reference<T> reference;
        private T proxy;

        public ConsumerBootstrap(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
        }

        public ConsumerBootstrap<T> setSerializationType(String serializationType) {
            protocolConfig.setSerializationType(serializationType);
            return this;
        }

        public ConsumerBootstrap<T> setCodecType(String codecType) {
            protocolConfig.setCodecType(codecType);
            return this;
        }

        public ConsumerBootstrap<T> setEndpointType(String endpointType) {
            protocolConfig.setEndpointType(endpointType);
            return this;
        }

        public ConsumerBootstrap<T> setLoadBalanceType(String loadBalanceType) {
            protocolConfig.setLoadBalanceType(loadBalanceType);
            return this;
        }

        public ConsumerBootstrap<T> setClusterType(String clusterType) {
            protocolConfig.setClusterType(clusterType);
            return this;
        }

        public ConsumerBootstrap<T> setRetryTimes(int retryTimes) {
            protocolConfig.setRetryTimes(retryTimes);
            return this;
        }

        public ConsumerBootstrap<T> setRecoveryPeriod(int recoveryPeriod) {
            protocolConfig.setRecoveryPeriod(recoveryPeriod);
            return this;
        }

        public ConsumerBootstrap<T> setHeartbeatPeriod(int heartbeatPeriod) {
            protocolConfig.setHeartbeatPeriod(heartbeatPeriod);
            return this;
        }

        public ConsumerBootstrap<T> addAddress(String address) {
            clientConfigBuilder.addAddress(address);
            return this;
        }

        public ConsumerBootstrap<T> timeout(int timeout) {
            clientConfigBuilder.timeout(timeout);
            return this;
        }

        public T getProxy() {
            if (proxy == null) {
                clientConfig = clientConfigBuilder.build();
                reference = new Reference<>();
                reference.setClientConfig(clientConfig);
                reference.setProtocolConfig(protocolConfig);
                reference.setInterfaceClass(interfaceClass);
                proxy = reference.refer();
            }
            return proxy;
        }

        public void destroyProxy() {
            if (reference != null) {
                reference.derefer();
            }
        }
    }

    public static class ProviderBootstrap {

        private ProtocolConfig protocolConfig = ProtocolConfig.defaultConfig();
        private ServerConfig.ServerConfigBuilder serverConfigBuilder = ServerConfig.builder();
        private ServerConfig serverConfig;
        private Exporter exporter;

        public ProviderBootstrap setSerializationType(String serializationType) {
            protocolConfig.setSerializationType(serializationType);
            return this;
        }

        public ProviderBootstrap setCodecType(String codecType) {
            protocolConfig.setCodecType(codecType);
            return this;
        }

        public ProviderBootstrap setEndpointType(String endpointType) {
            protocolConfig.setEndpointType(endpointType);
            return this;
        }

        public ProviderBootstrap port(int port) {
            serverConfigBuilder.port(port);
            return this;
        }

        public ProviderBootstrap workerThreadNum(int workerThreadNum) {
            serverConfigBuilder.workerThreadNum(workerThreadNum);
            return this;
        }

        public ProviderBootstrap minWorkerThreadNum(int minWorkerThreadNum) {
            serverConfigBuilder.minWorkerThreadNum(minWorkerThreadNum);
            return this;
        }

        public ProviderBootstrap maxWorkerThreadNum(int maxWorkerThreadNum) {
            serverConfigBuilder.maxWorkerThreadNum(maxWorkerThreadNum);
            return this;
        }

        public Exporter getExporter() {
            serverConfig = serverConfigBuilder.build();
            exporter = new Exporter();
            exporter.setProtocolConfig(protocolConfig);
            exporter.setServerConfig(serverConfig);
            return exporter;
        }

        public void unset() {
            if (exporter != null) {
                exporter.unexport();
            }
        }
    }

    public static <T> ConsumerBootstrap<T> consumer(Class<T> interfaceClass) {
        return new ConsumerBootstrap<>(interfaceClass);
    }

    public static ProviderBootstrap provider() {
        return new ProviderBootstrap();
    }

    private CattyBootstrap() {
    }
}
