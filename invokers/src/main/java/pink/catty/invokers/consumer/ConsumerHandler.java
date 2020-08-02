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
import java.util.concurrent.TimeoutException;
import pink.catty.core.CattyException;
import pink.catty.core.RpcTimeoutException;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.MethodNotFoundException;
import pink.catty.core.invoker.frame.DefaultRequest;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.service.MethodModel;
import pink.catty.core.service.ServiceModel;
import pink.catty.core.utils.RequestIdGenerator;

public class ConsumerHandler<T>
    implements InvocationHandler {

  private final Consumer consumer;

  public ConsumerHandler(Consumer consumer) {
    this.consumer = consumer;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    ServiceModel<T> serviceModel = consumer.getMeta().getServiceModel();

    /*
     * check if method valid.
     */
    if (!serviceModel.getValidMethod().contains(method)) {
      throw new CattyException("Can not invoke local method: " + method.getName());
    }

    MethodModel methodModel = serviceModel.getMethodMeta(method);
    if (methodModel == null) {
      throw new MethodNotFoundException("Method is invalid, method: " + method.getName());
    }

    Request request = new DefaultRequest(RequestIdGenerator.next(),
        serviceModel.getServiceName(),
        methodModel.getName(),
        args,
        serviceModel,
        methodModel,
        proxy
    );

    Class<?> returnType = method.getReturnType();

    Response response = invoke(request);

    if (returnType == Void.TYPE && !methodModel.isNeedReturn()) {
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
//    int delay = request.getInvokedMethod().getTimeout();
//    if (delay <= 0) {
//      delay = request.getServiceModel().getTimeout();
//    }
//    if (delay <= 0) {
    int delay = 30 * 1000;
//    }

    try {
      response.await(delay, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      throw new RpcTimeoutException("Timeout, except: " + delay + " Info: " + request.toString(),
          e);
    }

    Object returnValue = response.getValue();
    if (returnValue instanceof Throwable
        && !methodModel.getReturnType().isAssignableFrom(returnValue.getClass())) {
      throw (Throwable) returnValue;
    }
    return response.getValue();
  }

  private Response invoke(Request request) {
    return consumer.invoke(request);
  }

  @SuppressWarnings("unchecked")
  public static <E> E getProxy(Consumer consumer) {
    ServiceModel<E> serviceModel = consumer.getMeta().getServiceModel();
    Class<E> clazz = serviceModel.getInterfaceClass();
    E proxy = (E) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
        new ConsumerHandler<E>(consumer));
    serviceModel.setTarget(proxy);
    return proxy;
  }
}
