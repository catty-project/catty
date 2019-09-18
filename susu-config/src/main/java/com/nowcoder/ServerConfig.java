package com.nowcoder;

import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.registry.Registry;
import com.nowcoder.router.DefaultRouter;
import com.nowcoder.router.Provider;
import com.nowcoder.utils.NetUtils;
import com.nowcoder.zk.ZookeeperRegistry;
import java.net.InetAddress;
import java.util.Map;


/**
 * @author zrj CreateDate: 2019/9/10
 */
public class ServerConfig<T> {

  private String protocol = "susu";

  private int port;

  private T ref;

  private Class<T> interfaceClass;

  private String interfaceName;

  private RegistryConfig registryConfig;

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

  public void setRef(T ref) {
    this.ref = ref;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setRegistryConfig(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
  }

  public void export() {
    String host = getLocalHostAddress(null);
    URL url = new URL(protocol, host, port, interfaceName);

    DefaultRouter router = new DefaultRouter(url);
    router.init();

    Provider<T> provider = new Provider<>(ref, interfaceClass);
    router.addNewProvider(url, provider);

    URL registryUrl = registryConfig.toURL();
    Registry registry = new ZookeeperRegistry(registryUrl);
    registry.register(url);
//    NettyServer nettyServer = new NettyServer(new SusuCodec(), url,
//        new Provider<>(ref, interfaceClass));
//    nettyServer.open();
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
