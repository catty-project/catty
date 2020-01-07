package io.catty.zk;

import io.catty.api.Registry;
import io.catty.meta.EndpointMetaInfo;
import io.catty.config.RegistryConfig;
import io.catty.exception.RegistryException;
import io.catty.meta.EndpointTypeEnum;
import io.catty.meta.MetaInfoEnum;
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

public class ZookeeperRegistry implements Registry {

  private static final String PATH_SEP = "/";
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
  }

  @Override
  public void open() {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
    client = CuratorFrameworkFactory.builder()
        .connectString(registryConfig.getAddress())
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
  public void register(EndpointMetaInfo metaInfo) {
    checkClientStatus();
    String path = buildPath(metaInfo);
    if (!exist(path)) {
      buildPath(path);
    }
    path += PATH_SEP + metaInfo.toString();
    ephemeralPath(path);
  }

  @Override
  public void unregister(EndpointMetaInfo metaInfo) {
    checkClientStatus();
    String path = buildPath(metaInfo) + PATH_SEP + metaInfo.toString();
    delete(path);
  }

  @Override
  public void subscribe(EndpointMetaInfo metaInfo, NotifyListener listener) {
    checkClientStatus();

    String path = buildPath(metaInfo);
    try {
      List<String> metaInfos = client.getChildren().forPath(path);
      listener.notify(registryConfig, metaInfos.stream()
          .map(s -> EndpointMetaInfo.parse(s, metaInfo.getEndpointTypeEnum()))
          .collect(Collectors.toList()));
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: getChildren error", e);
    }

    CuratorListener curatorListener = ((client0, event) -> {
      if (interested.contains(event.getType())) {
        if (event.getPath() != null && event.getPath().startsWith(path)) {
          List<String> metaInfos = event.getChildren();
          listener.notify(registryConfig, metaInfos.stream()
              .map(s -> EndpointMetaInfo.parse(s, metaInfo.getEndpointTypeEnum()))
              .collect(Collectors.toList()));
        }
      }
    });
    client.getCuratorListenable().addListener(curatorListener);
  }

  @Override
  public void unsubscribe(EndpointMetaInfo metaInfo, NotifyListener listener) {

  }


  /* Curator helper */

  private void checkClientStatus() {
    if (!isOpen()) {
      throw new RegistryException(
          "ZookeeperRegistry: registry unavailable, url: " + registryConfig.getAddress());
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
   * zookeeper path: /root: susu/group name/interface name/providers?consumer/url;
   *
   * buildPath :       /root: susu/group name/interface name/providers?consumer
   */
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

  private String buildPath(EndpointMetaInfo config) {
    String root = "catty";
    String path = config.getString(MetaInfoEnum.SERVER_NAME.toString());
    String serverOrClient;
    if(config.getEndpointTypeEnum() == EndpointTypeEnum.CLIENT) {
      serverOrClient = CONSUMERS;
    } else if(config.getEndpointTypeEnum() == EndpointTypeEnum.SERVER) {
      serverOrClient = PROVIDERS;
    } else {
      throw new IllegalArgumentException();
    }
    return root
        + PATH_SEP + path
        + PATH_SEP + serverOrClient;
  }

}
