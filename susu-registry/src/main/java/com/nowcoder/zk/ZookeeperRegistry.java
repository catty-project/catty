package com.nowcoder.zk;

import com.nowcoder.common.constants.SusuConstants;
import com.nowcoder.config.AllConfig.REGISTRY_CONFIG;
import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.exception.RegistryException;
import com.nowcoder.registry.NotifyListener;
import com.nowcoder.registry.Registry;
import com.nowcoder.config.RegistryConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

/**
 * @author zrj CreateDate: 2019/9/9
 */
public class ZookeeperRegistry implements Registry {

  private RegistryConfig registryConfig;
  private CuratorFramework client;
  private Map<String, Set<String>> registered;

  public ZookeeperRegistry(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
    registered = new ConcurrentHashMap<>();
  }

  @Override
  public void init() {
    // todo: RetryPolicy 支持配置
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
    if (registryConfig.getStringConfig(REGISTRY_CONFIG.USERNAME) != null
        && registryConfig.getStringConfig(REGISTRY_CONFIG.PASSWORD) != null) {
      // todo : 账号密码支持
    } else {
      client = CuratorFrameworkFactory.builder()
          .connectString(registryConfig.getStringConfig(REGISTRY_CONFIG.IP_PORT))
          .sessionTimeoutMs(10000)
          .retryPolicy(retryPolicy)
          .build();
    }

    client.start();
  }

  @Override
  public void destroy() {
    if (client != null) {
      client.close();
    }
  }

  @Override
  public boolean isAvailable() {
    if (client == null) {
      return false;
    }
    CuratorFrameworkState state = client.getState();
    return state == CuratorFrameworkState.STARTED;
  }

  @Override
  public void register(URL url) {
    checkClientStatus();
    String path = buildPathFromConfig(url);
    String dataStr = url.getIpPortString();
    Set<String> pathData = registered.getOrDefault(path, Collections.newSetFromMap(new ConcurrentHashMap<>()));
    pathData.add(dataStr);
    registered.putIfAbsent(path, pathData);

    if(!exist(path)) {
      buildPath(path);
    }
    setData(path, getPathDataBytes(pathData));
  }

  @Override
  public void unregister(URL url) {
    checkClientStatus();
    String path = buildPathFromConfig(url);
    String dataStr = url.getIpPortString();
    Set<String> pathData = registered.get(path);
    if(pathData == null || pathData.size() == 0 || !exist(path)) {
      // todo: log
      return;
    }
    pathData.remove(dataStr);
    setData(path, getPathDataBytes(pathData));
  }

  @Override
  public void subscribe(URL url, NotifyListener listener) {

  }

  @Override
  public void unsubscribe(URL url, NotifyListener listener) {

  }

  @Override
  public List<URL> discover(URL url) {
    return null;
  }


  /* Curator helper */

  private void checkClientStatus() {
    if (!isAvailable()) {
      throw new RegistryException("ZookeeperRegistry: registry unavailable, url: " + registryConfig
          .getStringConfig(REGISTRY_CONFIG.IP_PORT));
    }
  }

  private boolean exist(String path) {
    try {
      return client.checkExists().forPath(path) != null;
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: exist check error", e);
    }
  }

  private void buildPath(String path) {
    try {
      client.create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.PERSISTENT)
          .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
          .forPath(path);
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: buildPath error", e);
    }
  }

  private void setData(String path, byte[] data) {
    try {
      client.setData()
          .forPath(path, data);
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: buildPath error", e);
    }
  }

  private void delete(String path, byte[] data) {
    try {
      client.setData()
          .forPath(path, data);
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: buildPath error", e);
    }
  }

  private byte[] getPathDataBytes(Set<String> pathData) {
    if(pathData == null || pathData.size() == 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for(String str : pathData) {
      sb.append(str).append(",");
    }
    if(sb.length() > 0) {
      sb.setLength(sb.length() - ",".length());
    }
    return sb.toString().getBytes();
  }



  private String buildPathFromConfig(URL url) {
    String root = registryConfig.getStringConfig(REGISTRY_CONFIG.ROOT);
    String group = url.getStringConfig(URL_CONFIG.GROUP);
    String path = url.getPath();
    String serverOrClient = url.getBooleanConfig(URL_CONFIG.IS_SERVER) ? "providers" : "consumer";
    return root + SusuConstants.PATH_SEP + group + SusuConstants.PATH_SEP + path
        + SusuConstants.PATH_SEP + serverOrClient;
  }

}
