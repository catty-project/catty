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
package pink.catty.core.config;

import java.util.Objects;
import pink.catty.core.ServerAddress;
import pink.catty.core.utils.NetUtils;

public class InnerServerConfig {

  public static InnerServerConfigBuilder builder() {
    return  new InnerServerConfigBuilder();
  }

  private final int port;
  private final int workerThreadNum;
  private final ServerAddress address;
  private final boolean needOrder;
  private final String codecType;

  private InnerServerConfig(int port, int workerThreadNum, ServerAddress address, boolean needOrder,
      String codecType) {
    this.port = port;
    this.workerThreadNum = workerThreadNum;
    this.needOrder = needOrder;
    this.codecType = codecType;
    if(address == null) {
      String serverIp = NetUtils.getLocalAddress().getHostAddress();
      this.address = new ServerAddress(serverIp, port);
    } else {
      this.address = address;
    }
  }

  public int getPort() {
    return port;
  }

  public int getWorkerThreadNum() {
    return workerThreadNum;
  }

  public boolean isNeedOrder() {
    return needOrder;
  }

  public ServerAddress getServerAddress() {
    return address;
  }

  public String getCodecType() {
    return codecType;
  }

  /**
   * builder
   */
  public static class InnerServerConfigBuilder {
    private int port;
    private int workerThreadNum;
    private ServerAddress address;
    private boolean needOrder;
    private String codecType;

    public InnerServerConfigBuilder port(int port) {
      this.port = port;
      return this;
    }

    public InnerServerConfigBuilder workerThreadNum(int workerThreadNum) {
      this.workerThreadNum = workerThreadNum;
      return this;
    }

    public InnerServerConfigBuilder address(ServerAddress address) {
      this.address = address;
      return this;
    }

    public InnerServerConfigBuilder needOrder(boolean needOrder) {
      this.needOrder = needOrder;
      return this;
    }

    public InnerServerConfigBuilder codecType(String codecType) {
      this.codecType = codecType;
      return this;
    }

    public InnerServerConfig build() {
      return new InnerServerConfig(port, workerThreadNum, address, needOrder, codecType);
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InnerServerConfig that = (InnerServerConfig) o;
    return port == that.port &&
        workerThreadNum == that.workerThreadNum &&
        needOrder == that.needOrder &&
        Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port, workerThreadNum, address, needOrder);
  }

  @Override
  public String toString() {
    return "InnerServerConfig{" +
        "port=" + port +
        ", workerThreadNum=" + workerThreadNum +
        ", address=" + address +
        ", needOrder=" + needOrder +
        ", codecType='" + codecType + '\'' +
        '}';
  }
}
