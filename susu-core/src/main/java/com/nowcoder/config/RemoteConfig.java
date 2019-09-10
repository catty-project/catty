package com.nowcoder.config;

import com.nowcoder.exception.SusuException;

/**
 * @author zrj CreateDate: 2019/9/8
 */
public class RemoteConfig extends ConfigCenter {

  private final boolean isServer;

  public RemoteConfig(boolean isServer) {
    this.isServer = isServer;
    if(isServer) {
      for(SERVER_CONFIG config0 : SERVER_CONFIG.values()) {
        if(config0.getDefaultValue() == null) {
          continue;
        }
        config.putIfAbsent(config0.getKey(), config0.getDefaultValue());
      }
    } else {
      for(CLIENT_CONFIG config0 : CLIENT_CONFIG.values()) {
        if(config0.getDefaultValue() == null) {
          continue;
        }
        config.putIfAbsent(config0.getKey(), config0.getDefaultValue());
      }
    }
  }

  public boolean isServer() {
    return isServer;
  }

  /* --- */

  public boolean getBooleanConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.getKey());
    return Boolean.valueOf(value);
  }

  public short getShortConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.getKey());
    return Short.valueOf(value);
  }

  public int getIntConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.getKey());
    return Integer.valueOf(value);
  }

  public long getLongConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.getKey());
    return Long.valueOf(value);
  }

  public String getStringConfig(CLIENT_CONFIG key) {
    assertClient();
    return getValue(key.getKey());
  }

  public Double getDoubleConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.getKey());
    return Double.valueOf(value);
  }

  /* --- */

  public boolean getBooleanConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.getKey());
    return Boolean.valueOf(value);
  }

  public short getShortConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.getKey());
    return Short.valueOf(value);
  }

  public int getIntConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.getKey());
    return Integer.valueOf(value);
  }

  public long getLongConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.getKey());
    return Long.valueOf(value);
  }

  public String getStringConfig(SERVER_CONFIG key) {
    assertServer();
    return getValue(key.getKey());
  }

  public Double getDoubleConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.getKey());
    return Double.valueOf(value);
  }

  public void setConfig(String key, String value) {
    config.put(key, value);
  }

  public void setConfig(SERVER_CONFIG key, String value) {
    assertServer();
    config.put(key.getKey(), value);
  }

  public void setConfig(CLIENT_CONFIG key, String value) {
    assertClient();
    config.put(key.getKey(), value);
  }

  /* private */
  private void assertClient(){
    if(isServer) {
      throw new SusuException("RemoteConfig: this config is a server config, so can't get client config");
    }
  }

  private void assertServer(){
    if(!isServer) {
      throw new SusuException("RemoteConfig: this config is a client config, so can't get server config");
    }
  }

}
