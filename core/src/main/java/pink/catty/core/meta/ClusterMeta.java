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

public class ClusterMeta extends ConsumerMeta {

  private String loadBalance;
  private int retryTimes;
  private int recoveryPeriod = 3 * 1000;

  public ClusterMeta() {
    super(MetaType.CLUSTER);
  }

  protected ClusterMeta(MetaType metaType) {
    super(metaType);
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
}
