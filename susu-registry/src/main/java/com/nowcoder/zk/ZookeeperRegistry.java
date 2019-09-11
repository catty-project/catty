package com.nowcoder.zk;

import com.nowcoder.common.constants.SusuConstants;
import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.exception.RegistryException;
import com.nowcoder.registry.NotifyListener;
import com.nowcoder.registry.Registry;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

/**
 * @author zrj CreateDate: 2019/9/9
 */
public class ZookeeperRegistry implements Registry {

  private static final String PROVIDERS = "providers";
  private static final String CONSUMERS = "consumers";
  private static final Set<CuratorEventType> interested = new HashSet<>();

  static {
    // 只关心创建节点和删除节点
    interested.add(CuratorEventType.CREATE);
    interested.add(CuratorEventType.DELETE);
  }

  private URL registryUrl;
  private CuratorFramework client;
  private Map<String, Set<String>> registered;

  public ZookeeperRegistry(URL url) {
    registryUrl = url;
    registered = new ConcurrentHashMap<>();
    init();
  }

  @Override
  public void init() {
    // todo: RetryPolicy 支持配置
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
    // todo : 账号密码支持
    client = CuratorFrameworkFactory.builder()
        .connectString(registryUrl.getIpPortString())
        .sessionTimeoutMs(10000)
        .retryPolicy(retryPolicy)
        .build();

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
    String path = buildPath(url);
    if (!exist(path)) {
      buildPath(path);
    }
    path += SusuConstants.PATH_SEP + encodeUrl(url.getUrlString());
    ephemeralPath(path);
  }

  @Override
  public void unregister(URL url) {
    checkClientStatus();
    String path = buildPath(url) + SusuConstants.PATH_SEP + url.getUrlString();
    delete(path);
  }

  @Override
  public void subscribe(URL url, NotifyListener listener) {
    checkClientStatus();
    String path = buildServerPath(url);

    // 先同步的回调一次
    try {
      List<String> urls = client.getChildren().forPath(buildServerPath(url) + SusuConstants.PATH_SEP + PROVIDERS);
      listener.notify(this.registryUrl, urls.stream()
          .map(this::decodeUrl)
          .map(URL::parse)
          .collect(Collectors.toList()));
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: getChildren error", e);
    }

    // 注册监听器
    CuratorListener curatorListener = ((client0, event) -> {
      if (interested.contains(event.getType())) {
        if (event.getPath() != null && event.getPath().startsWith(path)) {
          System.out.println(event);// todo: 调试删除
          List<String> urls = event.getChildren();
          listener.notify(this.registryUrl, urls.stream()
              .map(this::decodeUrl)
              .map(URL::parse)
              .collect(Collectors.toList()));
        }
      }
    });
    client.getCuratorListenable().addListener(curatorListener);
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
      throw new RegistryException(
          "ZookeeperRegistry: registry unavailable, url: " + registryUrl.getIpPortString());
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

  private void ephemeralPath(String path) {
    try {
      client.create()
          .withMode(CreateMode.EPHEMERAL)
          .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
          .forPath(path);
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: ephemeralPath error", e);
    }
  }

  private void delete(String path) {
    try {
      client.delete()
          .guaranteed()
          .forPath(path);
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: buildPath error", e);
    }
  }

//  @Deprecated
//  private byte[] getPathDataBytes(Set<String> pathData) {
//    if (pathData == null || pathData.size() == 0) {
//      return null;
//    }
//    StringBuilder sb = new StringBuilder();
//    for (String str : pathData) {
//      sb.append(str).append(",");
//    }
//    if (sb.length() > 0) {
//      sb.setLength(sb.length() - ",".length());
//    }
//    return sb.toString().getBytes();
//  }


  /**
   * zookeeper结构: /root: susu/group name/interface name/providers?consumer/url
   *
   * buildServerPath : /root: susu/group name/interface name
   *
   * buildPath :       /root: susu/group name/interface name/providers?consumer
   */
  private String buildServerPath(URL url) {
    String root = url.getString(URL_CONFIG.ROOT);
    String group = url.getString(URL_CONFIG.GROUP);
    String path = url.getPath();
    return root
        + SusuConstants.PATH_SEP + group
        + SusuConstants.PATH_SEP + path;
  }

  private String buildPath(URL url) {
    String root = url.getString(URL_CONFIG.ROOT);
    String group = url.getString(URL_CONFIG.GROUP);
    String path = url.getPath();
    String serverOrClient = url.getBoolean(URL_CONFIG.IS_SERVER) ? PROVIDERS : CONSUMERS;
    return root
        + SusuConstants.PATH_SEP + group
        + SusuConstants.PATH_SEP + path
        + SusuConstants.PATH_SEP + serverOrClient;
  }

  private String encodeUrl(String url) {
    return URL.encode(url);
  }

  private String decodeUrl(String url) {
    return URL.decode(url);
  }

}
