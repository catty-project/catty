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

public class ClientMeta extends EndpointMeta {

  private int timeout;
  private String loadBalance;
  private String healthCheckPeriod;
  private int retryTimes;
  private String endpoint;
  private int recoveryPeriod;

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getLoadBalance() {
    return loadBalance;
  }

  public void setLoadBalance(String loadBalance) {
    this.loadBalance = loadBalance;
  }

  public String getHealthCheckPeriod() {
    return healthCheckPeriod;
  }

  public void setHealthCheckPeriod(String healthCheckPeriod) {
    this.healthCheckPeriod = healthCheckPeriod;
  }

  public int getRetryTimes() {
    return retryTimes;
  }

  public void setRetryTimes(int retryTimes) {
    this.retryTimes = retryTimes;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public int getRecoveryPeriod() {
    return recoveryPeriod;
  }

  public void setRecoveryPeriod(int recoveryPeriod) {
    this.recoveryPeriod = recoveryPeriod;
  }
}
