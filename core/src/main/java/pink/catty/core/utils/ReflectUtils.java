/*
 * Copyright 2019 The Catty Project
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
package pink.catty.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import pink.catty.core.CattyException;

public abstract class ReflectUtils {

  private static final ConcurrentMap<String, Class<?>> nameToClassCache = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, Method> nameToMethodCache = new ConcurrentHashMap<>();

  private static final String EMPTY_PARAM = "void";
  private static final String PARAM_CLASS_SPLIT = ",";
  private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

  static {
    nameToClassCache.put("boolean", boolean.class);
    nameToClassCache.put("byte", byte.class);
    nameToClassCache.put("char", char.class);
    nameToClassCache.put("short", short.class);
    nameToClassCache.put("int", int.class);
    nameToClassCache.put("long", long.class);
    nameToClassCache.put("float", float.class);
    nameToClassCache.put("double", double.class);
    nameToClassCache.put("void", void.class);
  }

  public static List<Method> getPublicMethod(Class clazz) {
    Method[] methods = clazz.getMethods();
    List<Method> ret = new ArrayList<>();
    for (Method method : methods) {
      boolean isPublic = Modifier.isPublic(method.getModifiers());
      boolean isNotObjectClass = method.getDeclaringClass() != Object.class;
      if (isPublic && isNotObjectClass) {
        ret.add(method);
      }
    }
    return ret;
  }

  public static Class<?> forName(String className) throws ClassNotFoundException {
    if (null == className || "".equals(className)) {
      throw new ClassNotFoundException("class name: " + className);
    }

    Class<?> clz = nameToClassCache.get(className);
    if (clz != null) {
      return clz;
    }
    clz = forNameWithoutCache(className);
    nameToClassCache.putIfAbsent(className, clz);
    return clz;
  }

  public static Class<?>[] forNames(String classList) throws ClassNotFoundException {
    if (classList == null || "".equals(classList) || EMPTY_PARAM.equals(classList)) {
      return EMPTY_CLASS_ARRAY;
    }

    String[] classNames = classList.split(PARAM_CLASS_SPLIT);
    Class<?>[] classTypes = new Class<?>[classNames.length];
    for (int i = 0; i < classNames.length; i++) {
      String className = classNames[i];
      classTypes[i] = forName(className);
    }
    return classTypes;
  }

  /**
   * Get the signature of the method. Taking this method as example, this method's signature is:
   * "getMethodSign(java.lang.reflect.Method)".
   */
  public static String getMethodSign(Method method) {
    StringBuilder sb = new StringBuilder();
    sb.append(method.getName()).append("(");
    if (method.getParameterCount() > 0) {
      for (Class c : method.getParameterTypes()) {
        sb.append(c.getName()).append(",");
      }
      sb.setLength(sb.length() - ",".length());
    }
    sb.append(")");
    return sb.toString();
  }

  public static String getMethodSign(String methodName, String paramDesc) {
    if (paramDesc == null || "".equals(paramDesc)) {
      return methodName + "()";
    } else {
      return methodName + "(" + paramDesc + ")";
    }
  }

  public static <T> T getInstanceFromClass(Class<T> tClass) {
    try {
      Constructor<? extends T> constructor = tClass.getConstructor();
      if(!constructor.isAccessible()) {
        constructor.setAccessible(true);
      }
      return constructor.newInstance();
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new CattyException(e);
    }
  }

  public static Object convertFromString(Class<?> required, String value) {
    if(required == String.class) {
      return value;
    }
    if(required == boolean.class || required == Boolean.class) {
      return Boolean.valueOf(value);
    }
    if(required == byte.class || required == Byte.class) {
      return Byte.valueOf(value);
    }
    if(required == char.class || required == Character.class) {
      if(value.length() == 1) {
        return value.charAt(0);
      }
    }
    if(required == short.class || required == Short.class) {
      return Short.valueOf(value);
    }
    if(required == int.class || required == Integer.class) {
      return Integer.valueOf(value);
    }
    if(required == long.class || required == Long.class) {
      return Long.valueOf(value);
    }
    if(required == float.class || required == Float.class) {
      return Float.valueOf(value);
    }
    if(required == double.class || required == Double.class) {
      return Double.valueOf(value);
    }
    return null;
  }

  private static Class<?> forNameWithoutCache(String className) throws ClassNotFoundException {
    if (!className.endsWith("[]")) {
      return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    }

    int dimensionSiz = 0;
    while (className.endsWith("[]")) {
      dimensionSiz++;
      className = className.substring(0, className.length() - 2);
    }
    int[] dimensions = new int[dimensionSiz];
    Class<?> clz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    return Array.newInstance(clz, dimensions).getClass();
  }
}
