package io.catty.config;

import java.util.HashMap;
import java.util.Map;

public class GenericConfig {

  private Map<String, String> config = new HashMap<>();

  public void setConfig(String key, String value) {
    config.put(key, value);
  }

  public byte getByte(String key) {
    return Byte.valueOf(config.get(key));
  }

  public short getShort(String key) {
    return Short.valueOf(config.get(key));
  }

  public int getInt(String key) {
    return Integer.valueOf(config.get(key));
  }

  public long getLong(String key) {
    return Long.valueOf(config.get(key));
  }

  public String getString(String key) {
    return config.get(key);
  }

  public boolean getBool(String key) {
    return Boolean.valueOf(config.get(key));
  }

  public double getDouble(String key) {
    return Double.valueOf(config.get(key));
  }

  public byte getByteDef(String key, byte def) {
    if(config.containsKey(key)) {
      return Byte.valueOf(config.get(key));
    }
    return def;
  }

  public short getShortDef(String key, short def) {
    if(config.containsKey(key)) {
      return Short.valueOf(config.get(key));
    }
    return def;
  }

  public int getIntDef(String key, int def) {
    if(config.containsKey(key)) {
      return Integer.valueOf(config.get(key));
    }
    return def;
  }

  public long getLongDef(String key, long def) {
    if(config.containsKey(key)) {
      return Long.valueOf(config.get(key));
    }
    return def;
  }

  public String getStringDef(String key, String def) {
    if(config.containsKey(key)) {
      return config.get(key);
    }
    return def;
  }

  public boolean getBoolDef(String key, boolean def) {
    if(config.containsKey(key)) {
      return Boolean.valueOf(config.get(key));
    }
    return def;
  }

  public double getDoubleDef(String key, double def) {
    if(config.containsKey(key)) {
      return Double.valueOf(config.get(key));
    }
    return def;
  }

}
