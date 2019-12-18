package org.fire.core.config;

import lombok.Builder;
import org.fire.core.exception.IllegalAddressException;

@Builder
public class ClientConfig {

  private String address;

  private int timeout;

  public int getServerPort() {
    if (address.contains("://")) {
      address = address.substring(address.indexOf("://") + "://".length());
    }
    String[] ipPort = address.split(":");
    if (ipPort.length != 2) {
      throw new IllegalAddressException(
          "Multi ':' found in address, except one. Address: " + address);
    }
    try {
      return Integer.valueOf(ipPort[1]);
    } catch (NumberFormatException e) {
      throw new IllegalAddressException("Port is not Integer Type, Address: " + address, e);
    }
  }

  public String getServerIp() {
    if (address.contains("://")) {
      address = address.substring(address.indexOf("://") + "://".length());
    }
    String[] ipPort = address.split(":");
    if (ipPort.length != 2) {
      throw new IllegalAddressException(
          "Multi ':' found in address, except one. Address: " + address);
    }
    return ipPort[0];
  }

  public int getTimeout() {
    return timeout;
  }
}
