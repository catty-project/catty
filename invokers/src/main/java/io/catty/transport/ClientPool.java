package io.catty.transport;

import io.catty.core.Client;
import io.catty.core.config.ClientConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPool {

  private static Map<ClientConfig, Client> cache = new ConcurrentHashMap<>();

}
