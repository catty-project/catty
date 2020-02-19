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
package pink.catty.core;

import java.util.Objects;

public class ServerAddress {

  private String ip;
  private int port;
  private String address;

  public ServerAddress(String address) {
    this.address = address;
    if (address.contains("://")) {
      address = address.substring(address.indexOf("://") + "://".length());
    }
    String[] ipPort = address.split(":");
    if (ipPort.length != 2) {
      throw new IllegalAddressException(
          "Multi ':' found in address, except one. Address: " + address);
    }
    this.ip = ipPort[0];
    try {
      this.port = Integer.valueOf(ipPort[1]);
    } catch (NumberFormatException e) {
      throw new IllegalAddressException("Port is not Integer Type, Address: " + address, e);
    }
  }

  public ServerAddress(String ip, int port, String address) {
    this.ip = ip;
    this.port = port;
    this.address = address;
  }

  public ServerAddress(String ip, int port) {
    this.ip = ip;
    this.port = port;
    this.address = ip + ":" + port;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getAddress() {
    return address;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, port, address);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ServerAddress)) {
      return false;
    }
    ServerAddress other = (ServerAddress) obj;
    if (!Objects.equals(ip, other.ip)) {
      return false;
    }
    return Objects.equals(port, other.port);
  }

  @Override
  public String toString() {
    return ip + ":" + port;
  }
}
