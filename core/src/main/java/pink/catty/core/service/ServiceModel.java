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
package pink.catty.core.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pink.catty.core.utils.ReflectUtils;

/**
 * RpcService provider info.
 *
 * Cache service's interface info to mark an easy entry.
 */
public class ServiceModel<T> {

  private Class<T> interfaceClass;

  private Object target;

  private Map<String, Method> methodMap;

  private Set<Method> validMethod;

  private Map<Method, MethodModel> methodMetaMap;

  private String version = "";

  private String group = "";

  private String serviceName;

  private int timeout = -1;

  public static <T> ServiceModel<T> parse(Class<T> interfaceClass) {
    return new ServiceModel<>(interfaceClass);
  }

  private ServiceModel(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
    this.serviceName = interfaceClass.getName();
    this.methodMap = new HashMap<>();
    this.validMethod = new HashSet<>();
    this.methodMetaMap = new HashMap<>();

    List<Method> methods = ReflectUtils.getPublicMethod(interfaceClass);
    for (Method method : methods) {
      MethodModel methodModel = MethodModel.parse(method);

      // method's name
      String methodName = methodModel.getName();
      if (methodMap.containsKey(methodName)) {
        throw new DuplicatedMethodNameException(
            "Duplicated method name: " + methodName + "#" + method
                + ". Method's signature excepted unique.");
      }
      methodMap.put(methodName, method);

      // method's alias
      List<String> methodAlias = methodModel.getAlias();
      if(methodAlias != null && methodAlias.size() > 0) {
        for(String alias : methodAlias) {
          if (methodMap.containsKey(alias)) {
            throw new DuplicatedMethodNameException(
                "Duplicated method alias: " + alias + "#" + method
                    + ". Method's signature excepted unique.");
          }
          methodMap.put(alias, method);
        }
      }

      validMethod.add(method);
      methodMetaMap.put(method, methodModel);
    }

    if (interfaceClass.isAnnotationPresent(RpcService.class)) {
      RpcService serviceInfo = interfaceClass.getAnnotation(RpcService.class);
      this.version = serviceInfo.version();
      this.group = serviceInfo.group();
      if (!"".equals(serviceInfo.name())) {
        this.serviceName = serviceInfo.name();
      }
      this.timeout = serviceInfo.timeout();
    }
  }

  public Class<T> getInterfaceClass() {
    return interfaceClass;
  }

  public Set<Method> getValidMethod() {
    return validMethod;
  }

  public MethodModel getMethodMeta(Method method) {
    return methodMetaMap.get(method);
  }

  public void setTarget(Object target) {
    this.target = target;
  }

  public Object getTarget() {
    return target;
  }

  public String getVersion() {
    return version;
  }

  public String getGroup() {
    return group;
  }

  public String getServiceName() {
    return serviceName;
  }

  public int getTimeout() {
    return timeout;
  }

  public MethodModel getMethodMetaByName(String methodName) {
    Method method = methodMap.get(methodName);
    if (method == null) {
      return null;
    } else {
      return methodMetaMap.get(method);
    }
  }

  public Method getMethodByName(String methodName) {
    return methodMap.get(methodName);
  }

}
