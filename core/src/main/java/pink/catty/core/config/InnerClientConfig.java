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

public class InnerClientConfig {

  private final String ip;
  private final int port;
  private final String address;
  private final int timeout;
  private final String codecType;

  public InnerClientConfig(String ip, int port, String address, int timeout,
      String codecType) {
    this.ip = ip;
    this.port = port;
    this.address = address;
    this.timeout = timeout;
    this.codecType = codecType;
  }

  public int getServerPort() {
    return port;
  }

  public String getServerIp() {
    return ip;
  }

  public String getAddress() {
    return address;
  }

  public int getTimeout() {
    return timeout;
  }

  public String getCodecType() {
    return codecType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InnerClientConfig that = (InnerClientConfig) o;
    return port == that.port &&
        timeout == that.timeout &&
        Objects.equals(ip, that.ip) &&
        Objects.equals(address, that.address) &&
        Objects.equals(codecType, that.codecType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, port, address, timeout, codecType);
  }
}
