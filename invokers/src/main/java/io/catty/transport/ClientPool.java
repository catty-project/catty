package io.catty.transport;

import io.catty.core.Client;
import io.catty.core.config.InnerClientConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPool {

  private static Map<InnerClientConfig, Client> cache = new ConcurrentHashMap<>();

}
