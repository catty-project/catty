package io.catty;

import java.lang.reflect.Proxy;


public class ProxyFactory<T> {

  @SuppressWarnings("unchecked")
  public T getProxy(Class<T> clazz, Invoker invoker) {
    return (T) Proxy.newProxyInstance(
        clazz.getClassLoader(), new Class[]{clazz}, new ConsumerInvoker(clazz, invoker));
  }

}
