package pink.catty.core.config;

import java.util.Objects;
import pink.catty.core.ServerAddress;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InnerServerConfig that = (InnerServerConfig) o;
    return port == that.port &&
        workerThreadNum == that.workerThreadNum &&
        needOrder == that.needOrder &&
        Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port, workerThreadNum, address, needOrder);
  }
}
