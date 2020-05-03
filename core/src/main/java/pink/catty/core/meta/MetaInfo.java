/*
 * Copyright 2020 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.core.meta;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.utils.ReflectUtils;

public abstract class MetaInfo {

  protected static Logger logger = LoggerFactory.getLogger(MetaInfo.class);

  private static final String CUSTOM_META_MAP = "customMeta";

  public static <T extends MetaInfo> T parseOf(String metaString, Class<T> model) {
    Map<String, String> map = new HashMap<>();
    String[] metaInfoEntryArray = metaString.split(";");
    for (String entry : metaInfoEntryArray) {
      if (entry == null || "".equals(entry)) {
        continue;
      }
      map.put(entry.substring(0, entry.indexOf("=")), entry.substring(entry.indexOf("=") + 1));
    }

    T metaInfo = ReflectUtils.getInstanceFromClass(model);
    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(model);
    } catch (IntrospectionException e) {
      throw new MetaFormatException(e);
    }
    PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
    Map<String, PropertyDescriptor> descriptorMap = new HashMap<>();
    for (PropertyDescriptor descriptor : descriptors) {
      descriptorMap.put(descriptor.getName(), descriptor);
    }

    for (Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      PropertyDescriptor descriptor = descriptorMap.get(key);
      if (descriptor == null) {
        metaInfo.addCustomMeta(key, value);
        continue;
      }
      if(value == null || "".equals(value)) {
        continue;
      }
      Object typedValue = ReflectUtils.convertFromString(descriptor.getPropertyType(), value);
      if(typedValue == null) {
        continue;
      }
      Method method = descriptor.getWriteMethod();
      try {
        method.invoke(metaInfo, typedValue);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new MetaFormatException(e);
      }
    }

    return metaInfo;
  }

  @SuppressWarnings("unchecked")
  private static String toString(Object meta) {
    Class<?> clazz = meta.getClass();
    if (!EndpointMeta.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException(
          "Except type: <? extent pink.catty.core.provider.EndpointMeta>, but actual: " + meta
              .getClass().toString());
    }

    StringBuilder sb = new StringBuilder();
    List<Field> fieldList = new LinkedList<>();
    while (clazz != null && clazz != Object.class) {
      Field[] fields = clazz.getDeclaredFields();
      fieldList.addAll(Arrays.asList(fields));
      clazz = clazz.getSuperclass();
    }

    for (Field field : fieldList) {
      if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
        continue;
      }
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      if (field.getName().equals(CUSTOM_META_MAP)) {
        continue;
      }
      try {
        sb.append(field.getName()).append("=");
        if (field.get(meta) != null) {
          sb.append(field.get(meta));
        }
        sb.append(";");
      } catch (IllegalAccessException e) {
        logger.error("EndpointMeta toString access control error.", e);
      }
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length() - ";".length());
    }
    if (sb.length() <= 0) {
      return "";
    } else {
      return sb.toString();
    }
  }

  private MetaType metaType;
  private Map<String, String> customMeta = new HashMap<>();

  public MetaInfo(MetaType metaType) {
    this.metaType = metaType;
  }

  public MetaType getMetaType() {
    return metaType;
  }

  public void addCustomMeta(String key, Object value) {
    customMeta.put(key, String.valueOf(value));
  }

  public Map<String, String> getCustomMeta() {
    return customMeta;
  }

  public byte getByte(String key) {
    return getByteDef(key, (byte) 0);
  }

  public short getShort(String key) {
    return getShortDef(key, (short) 0);
  }

  public int getInt(String key) {
    return getIntDef(key, 0);
  }

  public long getLong(String key) {
    return getLongDef(key, 0L);
  }

  public String getString(String key) {
    return getStringDef(key, "");
  }

  public boolean getBool(String key) {
    return getBoolDef(key, false);
  }

  public double getDouble(String key) {
    return getDoubleDef(key, 0.0);
  }

  public byte getByteDef(String key, byte def) {
    if (customMeta.containsKey(key)) {
      return Byte.valueOf(customMeta.get(key));
    }
    return def;
  }

  public short getShortDef(String key, short def) {
    if (customMeta.containsKey(key)) {
      return Short.valueOf(customMeta.get(key));
    }
    return def;
  }

  public int getIntDef(String key, int def) {
    if (customMeta.containsKey(key)) {
      return Integer.valueOf(customMeta.get(key));
    }
    return def;
  }

  public long getLongDef(String key, long def) {
    if (customMeta.containsKey(key)) {
      return Long.valueOf(customMeta.get(key));
    }
    return def;
  }

  public String getStringDef(String key, String def) {
    if (customMeta.containsKey(key)) {
      return customMeta.get(key);
    }
    return def;
  }

  public boolean getBoolDef(String key, boolean def) {
    if (customMeta.containsKey(key)) {
      return Boolean.valueOf(customMeta.get(key));
    }
    return def;
  }

  public double getDoubleDef(String key, double def) {
    if (customMeta.containsKey(key)) {
      return Double.valueOf(customMeta.get(key));
    }
    return def;
  }

  @Override
  public String toString() {
    return toString(this);
  }

}
