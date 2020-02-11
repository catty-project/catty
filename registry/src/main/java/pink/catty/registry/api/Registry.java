package pink.catty.registry.api;

import pink.catty.core.meta.MetaInfo;
import java.util.List;

public interface Registry {

  void open();

  void close();

  boolean isOpen();

  void register(MetaInfo metaInfo);

  void unregister(MetaInfo metaInfo);

  void subscribe(MetaInfo metaInfo, NotifyListener listener);

  void unsubscribe(MetaInfo metaInfo, NotifyListener listener);

  interface NotifyListener {

    void notify(RegistryConfig registryConfig, List<MetaInfo> metaInfoCollection);
  }

}
