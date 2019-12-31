package org.fire.cluster;

import java.lang.reflect.Proxy;
import org.fire.core.Invoker;


public class ProxyFactory<T> {

  @SuppressWarnings("unchecked")
  public T getProxy(Class<T> clazz, Invoker invoker) {
    return (T) Proxy.newProxyInstance(
        clazz.getClassLoader(), new Class[]{clazz}, new InvokerProxyAdapter(clazz, invoker));
  }

}
