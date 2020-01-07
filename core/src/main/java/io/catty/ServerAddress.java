package io.catty;

import java.util.Objects;


public class ServerAddress {

  private String ip;
  private int port;

  public ServerAddress(String ip, int port) {
    this.ip = ip;
    this.port = port;
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
