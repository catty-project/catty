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
package pink.catty.core.meta;

import java.util.List;
import pink.catty.core.ServerAddress;
import pink.catty.core.service.ServiceModel;

public class ConsumerMeta extends ClientMeta {

  private static final int DEFAULT_HEALTH_CHECK_PERIOD = 10 * 1000;

  private transient ServiceModel serviceModel;

  private String version;
  private String group;
  private String serviceName;
  private int healthCheckPeriod = DEFAULT_HEALTH_CHECK_PERIOD;
  private List<ServerAddress> directAddress;
  private List<String> filterNames;

  /*
   * Cluster
   */
  private String cluster;
  private String loadBalance;
  private int retryTimes;
  private int recoveryPeriod = 3 * 1000;

  public ConsumerMeta() {
    super(MetaType.CONSUMER);
  }

  protected ConsumerMeta(MetaType metaType) {
    super(metaType);
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public int getHealthCheckPeriod() {
    return healthCheckPeriod;
  }

  public void setHealthCheckPeriod(int healthCheckPeriod) {
    this.healthCheckPeriod = healthCheckPeriod;
  }

  public ServiceModel getServiceModel() {
    return serviceModel;
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public String getLoadBalance() {
    return loadBalance;
  }

  public void setLoadBalance(String loadBalance) {
    this.loadBalance = loadBalance;
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

  public void setServiceModel(ServiceModel serviceModel) {
    this.serviceModel = serviceModel;
    this.version = serviceModel.getVersion();
    this.group = serviceModel.getGroup();
    this.serviceName = serviceModel.getServiceName();
  }

  public List<ServerAddress> getDirectAddress() {
    return directAddress;
  }

  public void setDirectAddress(List<ServerAddress> directAddress) {
    this.directAddress = directAddress;
  }

  public List<String> getFilterNames() {
    return filterNames;
  }

  public void setFilterNames(List<String> filterNames) {
    this.filterNames = filterNames;
  }
}
