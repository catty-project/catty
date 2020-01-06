package io.catty.zk;

import io.catty.api.Registry;
import io.catty.config.RegistryConfig;
import io.catty.config.ServerConfig;
import io.catty.exception.RegistryException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    interested.add(CuratorEventType.CREATE);
    interested.add(CuratorEventType.DELETE);
  }

  private RegistryConfig registryConfig;
  private CuratorFramework client;

  public ZookeeperRegistry(RegistryConfig registryConfig) {
    this.registryConfig = registryConfig;
    open();
  }

  @Override
  public void open() {
    // todo: RetryPolicy 支持配置
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
    // todo : 账号密码支持
    client = CuratorFrameworkFactory.builder()
        .connectString(registryConfig.getIpPortString())
        .sessionTimeoutMs(10000)
        .retryPolicy(retryPolicy)
        .build();

    client.start();
  }

  @Override
  public void close() {
    if (client != null) {
      client.close();
    }
  }

  @Override
  public boolean isOpen() {
    if (client == null) {
      return false;
    }
    CuratorFrameworkState state = client.getState();
    return state == CuratorFrameworkState.STARTED;
  }

  @Override
  public void register(ServerConfig serverConfig) {
    checkClientStatus();
    String path = buildPath(serverConfig);
    if (!exist(path)) {
      buildPath(path);
    }
    path += SusuConstants.PATH_SEP + encodeUrl(url.getUrlString());
    ephemeralPath(path);
  }

  @Override
  public void unregister(ServerConfig serverConfig) {
    checkClientStatus();
    String path = buildPath(url) + SusuConstants.PATH_SEP + url.getUrlString();
    delete(path);
  }

  @Override
  public void subscribe(ServerConfig serverConfig, NotifyListener listener) {
    checkClientStatus();
    String path = buildServerPath(url);

    try {
      List<String> urls = client.getChildren().forPath(buildServerPath(url) + SusuConstants.PATH_SEP + PROVIDERS);
      listener.notify(this.registryUrl, urls.stream()
          .map(this::decodeUrl)
          .map(URL::parse)
          .collect(Collectors.toList()));
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: getChildren error", e);
    }

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
  public void unsubscribe(ServerConfig serverConfig, NotifyListener listener) {

  }


  /* Curator helper */

  private void checkClientStatus() {
    if (!isOpen()) {
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


  /**
   * zookeeper path: /root: susu/group name/interface name/providers?consumer/url
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

  private String buildPath(ServerConfig config) {
    String root = "catty";
    String path = config.get;
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
