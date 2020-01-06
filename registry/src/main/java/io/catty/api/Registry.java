package io.catty.api;

import io.catty.config.RegistryConfig;
import io.catty.config.ServerConfig;

public interface Registry {

  void open();

  void close();

  boolean isOpen();

  void register(ServerConfig serverConfig);

  void unregister(ServerConfig serverConfig);

  void subscribe(ServerConfig serverConfig, NotifyListener listener);

  void unsubscribe(ServerConfig serverConfig, NotifyListener listener);

  interface NotifyListener {

    void notify(RegistryConfig registryConfig, String config);
  }

}
