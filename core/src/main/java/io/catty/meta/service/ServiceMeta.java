package io.catty.meta.service;

import io.catty.utils.ReflectUtils;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceMeta {

  private Class<?> interfaceClass;

  private Map<String, Method> methodMap;

  private Set<Method> validMethod;

  private Map<Method, MethodMeta> methodMetaMap;

  public ServiceMeta(Class<?> interfaceClass) {
    this.interfaceClass = interfaceClass;
    methodMap = new HashMap<>();
    validMethod = new HashSet<>();
    methodMetaMap = new HashMap<>();

    List<Method> methods = ReflectUtils.getPublicMethod(interfaceClass);
    for (Method method : methods) {
      String methodSign = ReflectUtils.getMethodDesc(method);
      if(methodMap.containsKey(methodSign)) {
        throw new Error("Duplicated method sign: " + methodSign + "#" + method
            + ". Method name excepted unique.");
      }
      methodMap.put(methodSign, method);
      validMethod.add(method);
    }
    // todo method meta
  }

  public Class<?> getInterfaceClass() {
    return interfaceClass;
  }

  public Set<Method> getValidMethod() {
    return validMethod;
  }

  public MethodMeta getMethodMeta(Method method) {
    return methodMetaMap.get(method);
  }

  public Method getMethodBySign(String methodSign) {
    return methodMap.get(methodSign);
  }

}