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

import pink.catty.core.extension.ExtensionType.ClusterType;
import pink.catty.core.extension.ExtensionType.CodecType;
import pink.catty.core.extension.ExtensionType.EndpointFactoryType;
import pink.catty.core.extension.ExtensionType.LoadBalanceType;
import pink.catty.core.extension.ExtensionType.SerializationType;

public class ProtocolConfig {

  private String serializationType = SerializationType.HESSIAN2;
  private String codecType = CodecType.CATTY;
  private String endpointType = EndpointFactoryType.NETTY;
  private String loadBalanceType = LoadBalanceType.RANDOM;
  private String clusterType = ClusterType.FAIL_FAST;
  private int retryTimes;
  private int recoveryPeriod;
  private int heartbeatPeriod;

  public static ProtocolConfig defaultConfig() {
    return new ProtocolConfig();
  }

  /**
   * {@link SerializationType}
   */
  public void setSerializationType(String serializationType) {
    this.serializationType = serializationType;
  }

  /**
   * {@link CodecType}
   */
  public void setCodecType(String codecType) {
    this.codecType = codecType;
  }

  /**
   * {@link EndpointFactoryType}
   */
  public void setEndpointType(String endpointType) {
    this.endpointType = endpointType;
  }

  /**
   * {@link EndpointFactoryType}
   */
  public void setLoadBalanceType(String loadBalanceType) {
    this.loadBalanceType = loadBalanceType;
  }

  public void setClusterType(String clusterType) {
    this.clusterType = clusterType;
  }

  public String getLoadBalanceType() {
    return loadBalanceType;
  }

  public String getClusterType() {
    return clusterType;
  }

  public String getSerializationType() {
    return serializationType;
  }

  public String getCodecType() {
    return codecType;
  }

  public String getEndpointType() {
    return endpointType;
  }

  public int getRetryTimes() {
    return retryTimes;
  }

  public void setRetryTimes(int retryTimes) {
    this.retryTimes = retryTimes;
  }

  public int getRecoveryPeriod() {
    return recoveryPeriod;
  }

  public void setRecoveryPeriod(int recoveryPeriod) {
    this.recoveryPeriod = recoveryPeriod;
  }

  public int getHeartbeatPeriod() {
    return heartbeatPeriod;
  }

  public void setHeartbeatPeriod(int heartbeatPeriod) {
    this.heartbeatPeriod = heartbeatPeriod;
  }
}
