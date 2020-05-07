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
package pink.catty.invokers.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import pink.catty.core.CattyException;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.MethodNotFoundException;
import pink.catty.core.invoker.cluster.Cluster;
import pink.catty.core.invoker.frame.DefaultRequest;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.service.MethodModel;
import pink.catty.core.service.ServiceModel;
import pink.catty.core.utils.RequestIdGenerator;

public class ConsumerHandler<T>
    implements InvocationHandler {

  private Cluster cluster;
  private Class<T> interfaceClazz;
  private ServiceModel serviceModel;

  public ConsumerHandler(ServiceModel<T> serviceModel, Cluster cluster) {
    this.cluster = cluster;
    this.interfaceClazz = serviceModel.getInterfaceClass();
    this.serviceModel = serviceModel;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    // todo: need better performance.
    if (isLocalMethod(method)) {
      throw new CattyException("Can not invoke local method: " + method.getName());
    }

    MethodModel methodModel = serviceModel.getMethodMeta(method);
    if (methodModel == null) {
      throw new MethodNotFoundException("Method is invalid, method: " + methodModel.getName());
    }

    Request request = new DefaultRequest();
    request.setRequestId(RequestIdGenerator.next());
    request.setInterfaceName(serviceModel.getServiceName());
    request.setMethodName(methodModel.getName());
    request.setArgsValue(args);

    Class<?> returnType = method.getReturnType();

    Invocation invocation = new Invocation();
    invocation.setInvokedMethod(methodModel);
    invocation.setTarget(proxy);
    invocation.setServiceModel(serviceModel);

    Response response = invoke(request, invocation);

    // todo: If void-return should wait for a response of TCP need be configurable.
    if (returnType == Void.TYPE) {
      return null;
    }
    // async-method
    if (methodModel.isAsync()) {
      CompletableFuture future = new CompletableFuture();
      response.whenComplete((v, t) -> {
        if (t != null) {
          future.completeExceptionally(t);
        } else {
          if (v instanceof Throwable
              && !v.getClass().isAssignableFrom(methodModel.getGenericReturnType())) {
            future.completeExceptionally((Throwable) v);
          } else {
            future.complete(v);
          }
        }
      });
      return future;
    }

    // sync-method
    int delay = invocation.getInvokedMethod().getTimeout();
    if (delay <= 0) {
      delay = invocation.getServiceModel().getTimeout();
    }

    if (delay > 0) {
      response.await(delay, TimeUnit.MILLISECONDS);
    } else {
      response.await(30 * 1000, TimeUnit.MILLISECONDS);
    }

    Object returnValue = response.getValue();
    if (returnValue instanceof Throwable
        && !methodModel.getReturnType().isAssignableFrom(returnValue.getClass())) {
      throw (Throwable) returnValue;
    }
    return response.getValue();
  }

  private Response invoke(Request request, Invocation invocation) {
    return cluster.invoke(request, invocation);
  }

  private boolean isLocalMethod(Method method) {
    if (method.getDeclaringClass().equals(Object.class)) {
      try {
        interfaceClazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        return false;
      } catch (NoSuchMethodException e) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static <E> E getProxy(ServiceModel serviceModel, Cluster cluster) {
    Class<E> clazz = serviceModel.getInterfaceClass();
    return (E) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
        new ConsumerHandler(serviceModel, cluster));
  }
}
