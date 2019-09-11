package com.nowcoder;

import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;

/**
 * @author zrj CreateDate: 2019/9/11
 */
public class RegistryConfig {

  private static final String disabled = "N/A";

  private String protocol = "zookeeper";

  private String address;

  public void setAddress(String address) {
    this.address = address;
    if (address.contains("://")) {
      protocol = address.substring(0, address.indexOf("://"));
    }
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public URL toURL() {
    if(!isValidAddress() || disabled.equals(address)) {
      return null;
    }
    URL url = new URL(protocol, address);
    url.setConfig(URL_CONFIG.ROOT, URL_CONFIG.ROOT.getDefaultValue());
    return url;
  }

  public boolean isValidAddress() {
    if(address == null || "".equals(address) || !address.contains(":")) {
      return false;
    }
    return true;
  }
}
