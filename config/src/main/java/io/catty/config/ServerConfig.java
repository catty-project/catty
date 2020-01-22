package io.catty.config;

import io.catty.utils.NetUtils;
import io.catty.ServerAddress;
import lombok.Builder;

@Builder
public class ServerConfig {

  private int port;

  private int workerThreadNum;

  /**
   * If every request from the same TCP should be executed by order, set this
   * option true.
   *
   * You should very carefully to set this option true. If you do so,
   * all requests from the same rpc-client would be executed one by one in
   * the same thread in rpc-server to guarantee to keep the invoking order,
   * as a consequence of severe performance.
   *
   * Actually, there are rarely conditions you should set this option true.
   */
  private boolean needOrder = false;

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
    String serverIp = NetUtils.getLocalAddress().getHostAddress();
    return new ServerAddress(serverIp, port);
  }

}
