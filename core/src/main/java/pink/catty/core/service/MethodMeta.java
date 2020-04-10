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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import pink.catty.core.utils.ReflectUtils;

public class MethodMeta {

  private String name;

  private List<String> alias;

  private Method method;

  private Map<String, Class<?>> checkedExceptions;

  /**
   * If CompletionStage is super-interface of return type, this method is an async method.
   *
   * NOTICE: Future interface could not imply an async method.
   */
  private boolean isAsync;

  /**
   * The plain return type.
   *
   * Map<?, ?> foo(); returnType == Map.class;
   *
   * List<?> foo(); returnType == List.class;
   *
   * int foo(); returnType == Integer.TYPE;
   */
  private Class<?> returnType;

  /**
   * The generic return type. Mainly used to get CompletionStage's generic return.
   *
   * There is more complicated situation like: Map<List<Object>, Future<T>> foo(); In this
   * case,Map.class will be returned.
   *
   * CompletionStage<Integer> foo(); genericReturnType == Integer.class;
   *
   * Otherwise, it is same as returnType.
   */
  private Class<?> futureGenericReturnType;

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
      if (!"".equals(function.name())) {
        this.name = function.name();
      }
      if (function.alias() != null && function.alias().length > 0) {
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
    return futureGenericReturnType;
  }

  public String getName() {
    return name;
  }

  public List<String> getAlias() {
    return alias;
  }

  public int getTimeout() {
    return timeout;
  }

  private void resolveReturnTypes(Method method) {
    Class<?> returnType = method.getReturnType();
    Type genericReturnType = method.getGenericReturnType();

    if (genericReturnType instanceof TypeVariable) {
      // T foo(); Not supported yet.
      throw new NotSupportedMethodException("TypeVariable is not supported yet");
    } else if (isAsync()) {
      // CompletionStage<?> foo();
      if (genericReturnType instanceof ParameterizedType) {
        Type actualArgType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        this.returnType = returnType;
        if (actualArgType instanceof ParameterizedType) {
          // CompletionStage<List<T>> foo();
          this.futureGenericReturnType = (Class<?>) ((ParameterizedType) actualArgType)
              .getRawType();
        } else if (actualArgType instanceof TypeVariable) {
          // CompletionStage<T> foo(); Not supported yet.
          throw new NotSupportedMethodException("TypeVariable is not supported yet");
        } else if (actualArgType instanceof GenericArrayType) {
          // CompletionStage<T[]> foo();
          this.futureGenericReturnType = (Class<?>) ((GenericArrayType) actualArgType)
              .getGenericComponentType();
        } else if (actualArgType instanceof Class<?>) {
          // CompletionStage<Object> foo();
          this.futureGenericReturnType = (Class<?>) actualArgType;
        } else {
          throw new NotSupportedMethodException(
              "GenericReturnType must be one of TypeVariable, ParameterizedType, GenericArrayType, Class. But now: "
                  + actualArgType.getTypeName());
        }
      } else {
        throw new NotSupportedMethodException(
            "CompletionStage return, genericReturnType must instanceof ParameterizedType");
      }
    } else {
      this.returnType = returnType;
      this.futureGenericReturnType = returnType;
    }
  }
}
