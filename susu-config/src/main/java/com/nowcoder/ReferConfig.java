package com.nowcoder;

import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.proxy.ProxyFactory;
import com.nowcoder.registry.Registry;
import com.nowcoder.utils.NetUtils;
import com.nowcoder.zk.ZookeeperRegistry;
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

  private RegistryConfig registryConfig;

  /**
   * direct connect.
   */
  private List<URL> direct;

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

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
  public void addAddress(String host, int port) {
    // interfaceName 为null则改方法不生效
    if(interfaceName == null) {
      return;
    }
    if(direct == null) {
      direct = new ArrayList<>();
    }
    URL url = new URL(protocol, host, port, interfaceName);
    url.setConfig(URL_CONFIG.IS_SERVER, String.valueOf(false));
    direct.add(url);
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public RegistryConfig getRegistryConfig() {
    return registryConfig;
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
    if(registryConfig == null) {
      throw new SusuException("ReferConfig: registryConfig can't be null");
    }

    String localAddress = getLocalHostAddress(null);
    URL url = new URL(protocol, localAddress, 0, interfaceName);
    url.setConfig(URL_CONFIG.IS_SERVER, false);
    url.setConfig(URL_CONFIG.VERSION, version);
    url.setConfig(URL_CONFIG.GROUP, group);

    URL registryUrl = registryConfig.toURL();
    if(registryUrl != null) {
      cluster = new DefaultCluster(url);
      Registry registry = new ZookeeperRegistry(registryUrl);
      registry.subscribe(url, cluster);
    } else if(direct != null && direct.size() > 0){
      cluster = new DefaultCluster(url, direct);
    } else {
      throw new SusuException("ReferConfig: registryConfig & direct can't both be null");
    }

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
