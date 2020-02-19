/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.extension.registry;

import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.Registry;
import pink.catty.core.config.RegistryConfig;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.RegistryException;
import pink.catty.core.meta.EndpointTypeEnum;
import pink.catty.core.meta.MetaInfoEnum;
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

@Extension("ZOOKEEPER")
public class ZookeeperRegistry implements Registry {

  private static final String ROOT = "/catty";
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
  public void register(MetaInfo metaInfo) {
    checkClientStatus();
    String path = buildPath(metaInfo, false);
    if (!exist(path)) {
      buildPath(path);
    }
    path += PATH_SEP + metaInfo.toString();
    ephemeralPath(path);
  }

  @Override
  public void unregister(MetaInfo metaInfo) {
    checkClientStatus();
    String path = buildPath(metaInfo, false) + PATH_SEP + metaInfo.toString();
    delete(path);
  }

  @Override
  public void subscribe(MetaInfo metaInfo, NotifyListener listener) {
    checkClientStatus();

    String path = buildPath(metaInfo, true);
    try {
      List<String> metaInfos = client.getChildren().forPath(path);
      listener.notify(registryConfig, metaInfos.stream()
          .map(s -> MetaInfo.parse(s, metaInfo.getEndpointTypeEnum()))
          .collect(Collectors.toList()));
    } catch (Exception e) {
      throw new RegistryException("ZookeeperRegistry: getChildren error", e);
    }

    CuratorListener curatorListener = ((client0, event) -> {
      if (interested.contains(event.getType())) {
        if (event.getPath() != null && event.getPath().startsWith(path)) {
          List<String> metaInfos = event.getChildren();
          listener.notify(registryConfig, metaInfos.stream()
              .map(s -> MetaInfo.parse(s, metaInfo.getEndpointTypeEnum()))
              .collect(Collectors.toList()));
        }
      }
    });
    client.getCuratorListenable().addListener(curatorListener);
  }

  @Override
  public void unsubscribe(MetaInfo metaInfo, NotifyListener listener) {

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
   * zookeeper path: /root: catty/group name/interface name/providers?consumer/meta;
   *
   * buildPath :     /root: catty/group name/interface name/providers?consumer
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

  private String buildPath(MetaInfo config, boolean isSubscribe) {
    String root = ROOT;
    String path = config.getString(MetaInfoEnum.SERVICE_NAME);
    String serverOrClient;
    if(isSubscribe) {
      serverOrClient = PROVIDERS;
    } else {
      if(config.getEndpointTypeEnum() == EndpointTypeEnum.CLIENT) {
        serverOrClient = CONSUMERS;
      } else if(config.getEndpointTypeEnum() == EndpointTypeEnum.SERVER) {
        serverOrClient = PROVIDERS;
      } else {
        throw new IllegalArgumentException();
      }
    }
    return root
        + PATH_SEP + path
        + PATH_SEP + serverOrClient;
  }

}
