package pink.catty.transport;

import pink.catty.core.Client;
import pink.catty.core.config.InnerClientConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPool {

  private static Map<InnerClientConfig, Client> cache = new ConcurrentHashMap<>();

}
