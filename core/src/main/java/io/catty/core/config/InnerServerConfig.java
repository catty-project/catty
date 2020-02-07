package io.catty.core.config;

import io.catty.core.ServerAddress;

public class InnerServerConfig {

  private final int port;
  private final int workerThreadNum;
  private final ServerAddress address;
  private final boolean needOrder;

  public InnerServerConfig(int port, int workerThreadNum, boolean needOrder, ServerAddress address) {
    this.port = port;
    this.workerThreadNum = workerThreadNum;
    this.needOrder = needOrder;
    this.address = address;
  }

  public int getPort() {
    return port;
  }

  public int getWorkerThreadNum() {
    return workerThreadNum;
  }

  public boolean isNeedOrder() {
    return needOrder;
  }

  public ServerAddress getServerAddress() {
    return address;
  }

}
