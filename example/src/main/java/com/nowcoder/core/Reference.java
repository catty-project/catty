package com.nowcoder.core;


import com.nowcoder.proxy.ProxyFactory;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class Reference<T> {

  private ProxyFactory<T> proxyFactory;

  private Class<T> interfaceClass;

  public Reference() {
    this.proxyFactory = new ProxyFactory<>();
  }

  public T getRefer() {
    return proxyFactory.getProxy(interfaceClass);
  }

  public Class<T> getInterfaceClass() {
    return interfaceClass;
  }

  public void setInterfaceClass(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }
}
