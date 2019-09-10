package com.nowcoder.config;

/**
 * 标识一个具体的注册中心的信息，包括路径信息
 *
 * @author zrj CreateDate: 2019/9/9
 */
public class RegistryConfig extends ConfigCenter {

  public RegistryConfig() {
    for (REGISTRY_CONFIG config0 : REGISTRY_CONFIG.values()) {
      if(config0.getDefaultValue() == null) {
        continue;
      }
      config.putIfAbsent(config0.getKey(), config0.getDefaultValue());
    }
  }

  /* registry */
  public boolean getBooleanConfig(REGISTRY_CONFIG key) {
    String value = getValue(key.getKey());
    return Boolean.valueOf(value);
  }

  public short getShortConfig(REGISTRY_CONFIG key) {
    String value = getValue(key.getKey());
    return Short.valueOf(value);
  }

  public int getIntConfig(REGISTRY_CONFIG key) {
    String value = getValue(key.getKey());
    return Integer.valueOf(value);
  }

  public long getLongConfig(REGISTRY_CONFIG key) {
    String value = getValue(key.getKey());
    return Long.valueOf(value);
  }

  public String getStringConfig(REGISTRY_CONFIG key) {
    return getValue(key.getKey());
  }

  public Double getDoubleConfig(REGISTRY_CONFIG key) {
    String value = getValue(key.getKey());
    return Double.valueOf(value);
  }

  public void setConfig(REGISTRY_CONFIG key, String value) {
    config.put(key.getKey(), value);
  }

}
