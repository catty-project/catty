package pink.catty.config;

import pink.catty.core.ServerAddress;
import pink.catty.core.config.InnerServerConfig;
import pink.catty.core.utils.NetUtils;

public class ServerConfig {

  public static ServerConfigBuilder builder() {
    return new ServerConfigBuilder();
  }

  private ServerConfig(int port, int workerThreadNum, boolean needOrder) {
    this.port = port;
    this.workerThreadNum = workerThreadNum;
    this.needOrder = needOrder;
    String serverIp = NetUtils.getLocalAddress().getHostAddress();
    this.address = new ServerAddress(serverIp, port);
  }

  private int port;

  private int workerThreadNum;

  private ServerAddress address;

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
    return address;
  }

  public InnerServerConfig toInnerConfig() {
    return new InnerServerConfig(port, workerThreadNum, needOrder, address);
  }

  /**
   * Builder
   */
  public static class ServerConfigBuilder {

    private int port;
    private int workerThreadNum;
    private boolean needOrder;

    public ServerConfigBuilder port(int port) {
      this.port = port;
      return this;
    }

    public ServerConfigBuilder workerThreadNum(int workerThreadNum) {
      this.workerThreadNum = workerThreadNum;
      return this;
    }

    public ServerConfigBuilder needOrder(boolean needOrder) {
      this.needOrder = needOrder;
      return this;
    }

    public ServerConfig build() {
      return new ServerConfig(port, workerThreadNum, needOrder);
    }
  }

}
