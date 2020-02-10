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
        Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, port, address, timeout);
  }
}
