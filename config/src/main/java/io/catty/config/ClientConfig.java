package io.catty.config;

import io.catty.core.IllegalAddressException;
import io.catty.core.config.InnerClientConfig;

public class ClientConfig {

  public static ClientConfigBuilder builder() {
    return new ClientConfigBuilder();
  }

  private ClientConfig(String ip, int port, String address, int timeout) {
    this.ip = ip;
    this.port = port;
    this.address = address;
    this.timeout = timeout;
  }

  private String ip;
  private int port;
  private String address;
  private int timeout;

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

  public InnerClientConfig toInnerConfig() {
    return new InnerClientConfig(ip, port, address, timeout);
  }

  /**
   * Builder
   */
  public static class ClientConfigBuilder {
    private String ip;
    private int port;
    private String address;
    private int timeout;

    public ClientConfigBuilder address(String address) {
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
      return this;
    }

    public ClientConfigBuilder timeout(int timeout) {
      this.timeout = timeout;
      return this;
    }

    public ClientConfig build() {
      return new ClientConfig(ip, port, address, timeout);
    }
  }
}
