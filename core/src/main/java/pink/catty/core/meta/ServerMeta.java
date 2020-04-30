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

public class ServerMeta extends EndpointMeta {

  private int workerThreadNum;
  private int minWorkerThreadNum;
  private int maxWorkerThreadNum;
  private boolean needOrder;

  public int getWorkerThreadNum() {
    return workerThreadNum;
  }

  public void setWorkerThreadNum(int workerThreadNum) {
    this.workerThreadNum = workerThreadNum;
  }

  public int getMinWorkerThreadNum() {
    return minWorkerThreadNum;
  }

  public void setMinWorkerThreadNum(int minWorkerThreadNum) {
    this.minWorkerThreadNum = minWorkerThreadNum;
  }

  public int getMaxWorkerThreadNum() {
    return maxWorkerThreadNum;
  }

  public void setMaxWorkerThreadNum(int maxWorkerThreadNum) {
    this.maxWorkerThreadNum = maxWorkerThreadNum;
  }

  public boolean isNeedOrder() {
    return needOrder;
  }

  public void setNeedOrder(boolean needOrder) {
    this.needOrder = needOrder;
  }
}
