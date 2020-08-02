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
import pink.catty.core.service.ServiceModel;
import pink.catty.core.utils.ReflectUtils;

/**
 * Meta info contains the whole structured information and information enters about an {@link
 * pink.catty.core.invoker.Invoker} which mains an Invoker could be described from a MetaInfo and
 * could be rebuild from a MetaInfo.
 *
 * This class is mainly for inner usage.
 *
 * As {@link pink.catty.core.invoker.Provider} and {@link pink.catty.core.invoker.Consumer} could be
 * registered to {@link pink.catty.core.extension.spi.Registry} and could be build from the
 * registered info, {@link MetaInfo} should be easily formatted to String or rebuild from String.
 * And as there are lots of uncertain subclass of this class, I use reflect to format every fields
 * (include parent classes) to String as "optionsA:a;optionB:b;.....optionN:n;" pattern, and also
 * use {@link Introspector} to rebuild MetaInfo from a String with auto-type-conversion.
 *
 * {@link MetaInfo} is an abstract class that supports custom meta info to ensure the scalability.
 *
 * Each field type should be simple enough to convert with String, if could not, it should be
 * treated specially in formatting and rebuilding.
 *
 * @see EndpointMeta
 * @see ClientMeta
 * @see ConsumerMeta
 * @see ConsumerMeta
 * @see ServerMeta
 * @see ProviderMeta
 *
 * @see MetaInfo#toString(MetaInfo)
 */
public abstract class MetaInfo {

  protected static Logger logger = LoggerFactory.getLogger(MetaInfo.class);

  private static final String CUSTOM_META_MAP = "customMeta";

  public static <T extends MetaInfo> T parseOf(String metaString, Class<T> model) {
    return parseOf(metaString, model, null);
  }

  public static <T extends MetaInfo> T parseOf(String metaString, Class<T> model,
      ServiceModel serviceModel) {
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
      if (value == null || "".equals(value)) {
        continue;
      }
      Object typedValue = ReflectUtils.convertFromString(descriptor.getPropertyType(), value);
      if (typedValue == null) {
        continue;
      }
      Method method = descriptor.getWriteMethod();
      try {
        if (!method.isAccessible()) {
          method.setAccessible(true);
        }
        method.invoke(metaInfo, typedValue);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new MetaFormatException(e);
      }
    }

    if (metaInfo instanceof ProviderMeta || metaInfo instanceof ConsumerMeta) {
      if (serviceModel == null) {
        return metaInfo;
      }
      if (metaInfo instanceof ProviderMeta) {
        ((ProviderMeta) metaInfo).setServiceModel(serviceModel);
      }
      if (metaInfo instanceof ConsumerMeta) {
        ((ConsumerMeta) metaInfo).setServiceModel(serviceModel);
      }
    }

    return metaInfo;
  }

  /**
   * each meta info has its own type.
   */
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

  @SuppressWarnings("unchecked")
  private static String toString(MetaInfo meta) {
    Class<?> clazz = meta.getClass();
    if (!MetaInfo.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException(
          "Except type: <? extent pink.catty.core.meta.MetaInfo>, but actual: " + meta
              .getClass().toString());
    }

    /*
     * get all parent's fields.
     */
    StringBuilder sb = new StringBuilder();
    List<Field> fieldList = new LinkedList<>();
    Class<?> parent = clazz;
    while (parent != null && parent != Object.class) {
      Field[] fields = parent.getDeclaredFields();
      fieldList.addAll(Arrays.asList(fields));
      parent = parent.getSuperclass();
    }

    /*
     * ignore static, transient, synthetic(this).
     */
    for (Field field : fieldList) {
      if (Modifier.isStatic(field.getModifiers())
          || Modifier.isTransient(field.getModifiers())
          || field.isSynthetic()) {
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
        logger.error("MetaInfo toString access control error.", e);
      }
    }

    /*
     * append custom meta.
     */
    for (Entry<String, String> entry : meta.getCustomMeta().entrySet()) {
      sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
    }

    if (sb.length() <= 0) {
      return "";
    } else {
      return sb.toString();
    }
  }
}
