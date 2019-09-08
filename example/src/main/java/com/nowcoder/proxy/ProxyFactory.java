package com.nowcoder.proxy;

import java.lang.reflect.Proxy;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ProxyFactory<T> {

  @SuppressWarnings("unchecked")
  public T getProxy(Class<T> clazz) {
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ProxyHandler());
  }

}
