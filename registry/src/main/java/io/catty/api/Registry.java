package io.catty.api;

import io.catty.meta.EndpointMetaInfo;
import java.util.List;

public interface Registry {

  void open();

  void close();

  boolean isOpen();

  void register(EndpointMetaInfo metaInfo);

  void unregister(EndpointMetaInfo metaInfo);

  void subscribe(EndpointMetaInfo metaInfo, NotifyListener listener);

  void unsubscribe(EndpointMetaInfo metaInfo, NotifyListener listener);

  interface NotifyListener {

    void notify(RegistryConfig registryConfig, List<EndpointMetaInfo> metaInfoCollection);
  }

}
