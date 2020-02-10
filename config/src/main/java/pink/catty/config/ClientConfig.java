package pink.catty.config;

import java.util.ArrayList;
import java.util.List;
import pink.catty.core.CattyException;
import pink.catty.core.ServerAddress;

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
