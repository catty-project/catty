package io.catty.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MetaInfo {

  private Map<String, String> info = new HashMap<>();

  private EndpointTypeEnum endpointTypeEnum;

  public MetaInfo(EndpointTypeEnum endpointTypeEnum) {
    this.endpointTypeEnum = endpointTypeEnum;
  }

  public MetaInfo(Map<String, String> info, EndpointTypeEnum endpointTypeEnum) {
    this.info = info;
    this.endpointTypeEnum = endpointTypeEnum;
  }

  public EndpointTypeEnum getEndpointTypeEnum() {
    return endpointTypeEnum;
  }

  /**
   * System setting style: setting1=a;setting2=b;setting3=c;
   * todo : need more robust
   */
  public static MetaInfo parse(String metaInfoStr, EndpointTypeEnum endpointTypeEnum) {
    Map<String, String> map = new HashMap<>();
    String[] metaInfoEntryArray = metaInfoStr.split(";");
    for(String entry : metaInfoEntryArray) {
      if(entry == null || "".equals(entry)) {
        continue;
      }
      map.put(entry.substring(0, entry.indexOf("=")), entry.substring(entry.indexOf("=") + 1));
    }
    return new MetaInfo(map, endpointTypeEnum);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(Entry<String, String> entry : info.entrySet()) {
      sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
    }
    return sb.toString();
  }

  public void addMetaInfo(MetaInfoEnum key, String value) {
    info.put(key.toString(), value);
  }

  public void addMetaInfo(MetaInfoEnum key, Object value) {
    info.put(key.toString(), String.valueOf(value));
  }

  public byte getByte(MetaInfoEnum key) {
    return Byte.valueOf(info.get(key.toString()));
  }

  public short getShort(MetaInfoEnum key) {
    return Short.valueOf(info.get(key.toString()));
  }

  public int getInt(MetaInfoEnum key) {
    return Integer.valueOf(info.get(key.toString()));
  }

  public long getLong(MetaInfoEnum key) {
    return Long.valueOf(info.get(key.toString()));
  }

  public String getString(MetaInfoEnum key) {
    return info.get(key.toString());
  }

  public boolean getBool(MetaInfoEnum key) {
    return Boolean.valueOf(info.get(key.toString()));
  }

  public double getDouble(MetaInfoEnum key) {
    return Double.valueOf(info.get(key.toString()));
  }

  public byte getByteDef(MetaInfoEnum key, byte def) {
    if(info.containsKey(key.toString())) {
      return Byte.valueOf(info.get(key.toString()));
    }
    return def;
  }

  public short getShortDef(MetaInfoEnum key, short def) {
    if(info.containsKey(key.toString())) {
      return Short.valueOf(info.get(key.toString()));
    }
    return def;
  }

  public int getIntDef(MetaInfoEnum key, int def) {
    if(info.containsKey(key.toString())) {
      return Integer.valueOf(info.get(key.toString()));
    }
    return def;
  }

  public long getLongDef(MetaInfoEnum key, long def) {
    if(info.containsKey(key.toString())) {
      return Long.valueOf(info.get(key.toString()));
    }
    return def;
  }

  public String getStringDef(MetaInfoEnum key, String def) {
    if(info.containsKey(key.toString())) {
      return info.get(key.toString());
    }
    return def;
  }

  public boolean getBoolDef(MetaInfoEnum key, boolean def) {
    if(info.containsKey(key.toString())) {
      return Boolean.valueOf(info.get(key.toString()));
    }
    return def;
  }

  public double getDoubleDef(MetaInfoEnum key, double def) {
    if(info.containsKey(key.toString())) {
      return Double.valueOf(info.get(key.toString()));
    }
    return def;
  }

  public void addCustomMetaInfo(String key, String value) {
    info.put(key, value);
  }

  public byte getCustomByte(String key) {
    return Byte.valueOf(info.get(key));
  }

  public short getCustomShort(String key) {
    return Short.valueOf(info.get(key));
  }

  public int getCustomInt(String key) {
    return Integer.valueOf(info.get(key));
  }

  public long getCustomLong(String key) {
    return Long.valueOf(info.get(key));
  }

  public String getCustomString(String key) {
    return info.get(key);
  }

  public boolean getCustomBool(String key) {
    return Boolean.valueOf(info.get(key));
  }

  public double getCustomDouble(String key) {
    return Double.valueOf(info.get(key));
  }

  public byte getCustomByteDef(String key, byte def) {
    if(info.containsKey(key)) {
      return Byte.valueOf(info.get(key));
    }
    return def;
  }

  public short getCustomShortDef(String key, short def) {
    if(info.containsKey(key)) {
      return Short.valueOf(info.get(key));
    }
    return def;
  }

  public int getCustomIntDef(String key, int def) {
    if(info.containsKey(key)) {
      return Integer.valueOf(info.get(key));
    }
    return def;
  }

  public long getCustomLongDef(String key, long def) {
    if(info.containsKey(key)) {
      return Long.valueOf(info.get(key));
    }
    return def;
  }

  public String getCustomStringDef(String key, String def) {
    if(info.containsKey(key)) {
      return info.get(key);
    }
    return def;
  }

  public boolean getCustomBoolDef(String key, boolean def) {
    if(info.containsKey(key)) {
      return Boolean.valueOf(info.get(key));
    }
    return def;
  }

  public double getCustomDoubleDef(String key, double def) {
    if(info.containsKey(key)) {
      return Double.valueOf(info.get(key));
    }
    return def;
  }

  @Override
  public int hashCode() {
    return info.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if(!(o instanceof MetaInfo)) {
      return false;
    }
    return info.equals(((MetaInfo) o).info);
  }

}
