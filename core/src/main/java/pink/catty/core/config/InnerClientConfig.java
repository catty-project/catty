package pink.catty.core.config;

public class InnerClientConfig {

  private final String ip;
  private final int port;
  private final String address;
  private final int timeout;

  public InnerClientConfig(String ip, int port, String address, int timeout) {
    this.ip = ip;
    this.port = port;
    this.address = address;
    this.timeout = timeout;
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

}
