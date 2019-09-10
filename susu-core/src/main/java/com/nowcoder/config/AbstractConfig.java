package com.nowcoder.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zrj CreateDate: 2019/9/9
 */
abstract public class AbstractConfig implements CustomConfig {

  protected Map<String, String> config;

  public AbstractConfig() {
    this.config = new ConcurrentHashMap<>();
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


  /**
   * Null safe method，并不需要，ConcurrentHashMap没有null，不过以后换Map实现后需要改这里。
   */
  protected String getValue(String key) {
    String value = config.get(key);
    return value;
  }
}
