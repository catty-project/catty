package io.catty.config;

import io.catty.core.CattyException;
import io.catty.core.ServerAddress;
import io.catty.core.config.InnerClientConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientConfig {

  public static ClientConfigBuilder builder() {
    return new ClientConfigBuilder();
  }

  private List<ServerAddress> addresses;
  private int timeout;

  private ClientConfig(List<ServerAddress> addresses, int timeout) {
    this.addresses = addresses;
    this.timeout = timeout;
  }

  public ServerAddress getFirstAddress() {
    if(addresses == null || addresses.size() <= 0) {
      throw new CattyException("No available address");
    }
    return addresses.get(0);
  }

  public List<ServerAddress> getAddresses() {
    if(addresses == null || addresses.size() <= 0) {
      throw new CattyException("No available address");
    }
    return addresses;
  }

  public int getTimeout() {
    return timeout;
  }

  public List<InnerClientConfig> toInnerConfig() {
    if(addresses == null || addresses.size() <= 0) {
      throw new CattyException("No available address");
    }
    return addresses.stream()
        .map(address -> new InnerClientConfig(address.getIp(), address.getPort(), address.getAddress(), timeout))
        .collect(Collectors.toList());
  }

  /**
   * Builder
   */
  public static class ClientConfigBuilder {
    private List<ServerAddress> addresses;
    private int timeout;

    public ClientConfigBuilder addAddress(String address) {
      if(addresses == null) {
        addresses = new ArrayList<>();
      }
      addresses.add(new ServerAddress(address));
      return this;
    }

    public ClientConfigBuilder timeout(int timeout) {
      this.timeout = timeout;
      return this;
    }

    public ClientConfig build() {
      return new ClientConfig(addresses, timeout);
    }
  }
}
