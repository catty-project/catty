package com.nowcoder;

import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.proxy.ProxyFactory;
import com.nowcoder.utils.NetUtils;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zrj CreateDate: 2019/9/10
 */
public class ReferConfig<T> {

  private ProxyFactory<T> proxyFactory = new ProxyFactory<>();

  private Class<T> interfaceClass;

  private String interfaceName;

  private T ref;

  private String protocol = "susu";

  private Cluster cluster;

  private String version;

  private String group;

  /**
   * direct connect.
   */
  private List<URL> urls;

  public void setInterfaceClass(Class<T> interfaceClass) {
    this.interfaceClass = interfaceClass;
    interfaceName = interfaceClass.getName();
  }

  @SuppressWarnings("unchecked")
  public void setInterfaceName(String interfaceName) throws ClassNotFoundException {
    this.interfaceName = interfaceName;
    interfaceClass = (Class<T>) Class
        .forName(interfaceName, false, Thread.currentThread().getContextClassLoader());
  }

  /**
   * 前期使用的简单方法，主要用来测试
   */
  public void addAddress(String address, int port) {
    // interfaceName 为null则改方法不生效
    if(interfaceName == null) {
      return;
    }
    if(urls == null) {
      urls = new ArrayList<>();
    }
    URL url = new URL(protocol, address, port, interfaceName);
    url.setConfig(URL_CONFIG.IS_SERVER, String.valueOf(false));
    urls.add(url);
  }

  /**
   * 前期使用的简单方法，主要用来测试
   */
  public void addAddress(URL url) {
    // interfaceName 为null则改方法不生效
    if(interfaceName == null) {
      return;
    }
    if(urls == null) {
      urls = new ArrayList<>();
    }
    urls.add(url);
  }

  public T getRefer() {
    if (ref == null) {
      init();
    }
    return ref;
  }

  private void init() {
    if(interfaceClass == null || interfaceName == null || "".equals(interfaceName)) {
      throw new SusuException("ReferConfig: server interface info is not complete");
    }
    String localAddress = getLocalHostAddress(null);
    URL url = new URL(protocol, localAddress, 0, interfaceName);
    cluster = new DefaultCluster(url, urls);
    ref = proxyFactory.getProxy(interfaceClass, url, cluster);
  }



  private String getLocalHostAddress(Map<String, Integer> remoteHostPorts) {
    String localAddress = null;
    InetAddress address = NetUtils.getLocalAddress(remoteHostPorts);
    if (address != null) {
      localAddress = address.getHostAddress();
    }

    if (NetUtils.isValidLocalHost(localAddress)) {
      return localAddress;
    }
    throw new SusuException("Please config local server hostname with intranet IP first!");
  }

}
