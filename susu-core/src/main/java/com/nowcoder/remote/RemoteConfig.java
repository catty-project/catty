package com.nowcoder.remote;

import com.nowcoder.exception.SusuException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zrj CreateDate: 2019/9/8
 */
public class RemoteConfig implements CustomConfig {

  public enum CLIENT_CONFIG {
    REMOTE_IP("serverIp", "127.0.0.1"),
    REMOTE_PORT("serverPort", "25880"),

    ;
    private String key;
    private String defaultValue;

    CLIENT_CONFIG(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }
  }

  public enum SERVER_CONFIG {
    SERVER_PORT("serverPort", "25880"),

    ;
    private String key;
    private String defaultValue;

    SERVER_CONFIG(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }
  }

  private Map<String, String> config;
  private final boolean isServer;

  public RemoteConfig(boolean isServer) {
    this.isServer = isServer;
    this.config = new ConcurrentHashMap<>();
    if(isServer) {
      for(SERVER_CONFIG config0 : SERVER_CONFIG.values()) {
        config.putIfAbsent(config0.key, config0.defaultValue);
      }
    } else {
      for(CLIENT_CONFIG config0 : CLIENT_CONFIG.values()) {
        config.putIfAbsent(config0.key, config0.defaultValue);
      }
    }


  }

  /**
   * 方便扩展自定义的Config
   */
  @Override
  public void addCustomConfig(String key, String value) {
    config.putIfAbsent(key, value);
  }

  @Override
  public String removeCustomConfig(String key) {
    return config.remove(key);
  }

  /* --- */

  public boolean getBooleanConfig(String key) {
    String value = getValue(key);
    return Boolean.valueOf(value);
  }

  public short getShortConfig(String key) {
    String value = getValue(key);
    return Short.valueOf(value);
  }

  public int getIntConfig(String key) {
    String value = getValue(key);
    return Integer.valueOf(value);
  }

  public long getLongConfig(String key) {
    String value = getValue(key);
    return Long.valueOf(value);
  }

  public String getStringConfig(String key) {
    return getValue(key);
  }

  public Double getDoubleConfig(String key) {
    String value = getValue(key);
    return Double.valueOf(value);
  }

  /* --- */

  public boolean getBooleanConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.key);
    return Boolean.valueOf(value);
  }

  public short getShortConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.key);
    return Short.valueOf(value);
  }

  public int getIntConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.key);
    return Integer.valueOf(value);
  }

  public long getLongConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.key);
    return Long.valueOf(value);
  }

  public String getStringConfig(CLIENT_CONFIG key) {
    assertClient();
    return getValue(key.key);
  }

  public Double getDoubleConfig(CLIENT_CONFIG key) {
    assertClient();
    String value = getValue(key.key);
    return Double.valueOf(value);
  }

  /* --- */

  public boolean getBooleanConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.key);
    return Boolean.valueOf(value);
  }

  public short getShortConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.key);
    return Short.valueOf(value);
  }

  public int getIntConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.key);
    return Integer.valueOf(value);
  }

  public long getLongConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.key);
    return Long.valueOf(value);
  }

  public String getStringConfig(SERVER_CONFIG key) {
    assertServer();
    return getValue(key.key);
  }

  public Double getDoubleConfig(SERVER_CONFIG key) {
    assertServer();
    String value = getValue(key.key);
    return Double.valueOf(value);
  }

  /**
   * Null safe method
   */
  private String getValue(String key) {
    String value = config.get(key);
    if(value == null) {
      throw new SusuException("RemoteConfig: unknown config key: " + key);
    }
    return value;
  }

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
