package com.nowcoder;

import com.nowcoder.core.URL;
import com.nowcoder.config.RegistryConfig;
import com.nowcoder.zk.ZookeeperRegistry;

/**
 * @author zrj CreateDate: 2019/9/9
 */
public class ZkTest {

  public static void main(String[] args) {
    RegistryConfig config = new RegistryConfig();

    URL url = new URL("susu", "127.0.0.1", 2181, "com.nowcoder.IServer");

    ZookeeperRegistry zookeeperRegistry = new ZookeeperRegistry(config);
    System.out.println(zookeeperRegistry.isAvailable());
    zookeeperRegistry.init();
    System.out.println(zookeeperRegistry.isAvailable());
    zookeeperRegistry.register(url);

  }

}
