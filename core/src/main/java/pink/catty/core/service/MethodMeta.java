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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import pink.catty.core.utils.ReflectUtils;

public class MethodMeta {

  private String name;

  private List<String> alias;

  private Method method;

  private Map<String, Class<?>> checkedExceptions;

  private boolean isAsync;

  private Class<?> returnType;

  private Class<?> genericReturnType;

  private int timeout = -1; /* -1 means no timeout */

  public static MethodMeta parse(Method method) {
    return new MethodMeta(method);
  }

  private MethodMeta(Method method) {
    this.method = method;
    this.name = ReflectUtils.getMethodSign(method);
    this.checkedExceptions = new HashMap<>();
    Arrays.stream(method.getExceptionTypes())
        .forEach(aClass -> checkedExceptions.put(aClass.getName(), aClass));
    this.returnType = method.getReturnType();

    /*
     * If CompletionStage is super-interface of return type, this method is an async method.
     *
     * NOTICE: Future interface could not imply an async method.
     */
    this.isAsync = CompletionStage.class.isAssignableFrom(returnType);
    resolveReturnTypes(method);

    if (method.isAnnotationPresent(RpcMethod.class)) {
      RpcMethod function = method.getDeclaredAnnotation(RpcMethod.class);
      this.timeout = function.timeout();
      if(!"".equals(function.name())) {
        this.name = function.name();
      }
      if(function.alias() != null && function.alias().length > 0) {
        alias = new ArrayList<>(Arrays.asList(function.alias()));
      }
    }
  }

  public Class<?> getCheckedExceptionByName(String className) {
    return checkedExceptions.get(className);
  }

  public Method getMethod() {
    return method;
  }

  public boolean isAsync() {
    return isAsync;
  }

  public boolean containsCheckedException(Class<?> exceptionClass) {
    return checkedExceptions.containsKey(exceptionClass.getName());
  }

  public boolean containsCheckedException(String exceptionClassName) {
    return checkedExceptions.containsKey(exceptionClassName);
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public Class<?> getGenericReturnType() {
    return genericReturnType;
  }

  public String getName() {
    return name;
  }

  public List<String> getAlias() {
    return alias;
  }

  // fixme : bug.
  private void resolveReturnTypes(Method method) {
    Class<?> returnType = method.getReturnType();
    Type genericReturnType = method.getGenericReturnType();
    if (Future.class.isAssignableFrom(returnType)) {
      if (genericReturnType instanceof ParameterizedType) {
        Type actualArgType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        if (actualArgType instanceof ParameterizedType) {
          returnType = (Class<?>) ((ParameterizedType) actualArgType).getRawType();
          genericReturnType = actualArgType;
        } else {
          returnType = (Class<?>) actualArgType;
          genericReturnType = returnType;
        }
      } else {
        returnType = null;
        genericReturnType = null;
      }
    }
    this.returnType = returnType;
    this.genericReturnType = (Class) genericReturnType;
  }

  public int getTimeout() {
    return timeout;
  }
}
