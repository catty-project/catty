package io.catty.transport;

import io.catty.GlobalConstants;
import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;
import io.catty.Invocation;
import io.catty.codec.Codec;
import io.catty.config.ServerConfig;
import io.catty.transport.worker.ConsistentHashLoopGroup;
import io.catty.transport.worker.HashableChooserFactory;
import io.catty.transport.worker.HashableExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServer implements Server {

  private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);

  private static final int NEW = 0;
  private static final int CONNECTED = 1;
  private static final int DISCONNECTED = 2;

  private ServerConfig serverConfig;
  private volatile int status = NEW;
  private Codec codec;
  private Invoker invoker;

  /**
   * HashableExecutor is needed, because if a request just be submitted randomly to a generic
   * executor such as ThreadPollExecutor, then the a series of requests can't be able to be executed
   * by the origin order even if they are transformed by the same TCP link. And in some cases, this
   * will cause severe problem.
   *
   * To fix this problem, we introduce HashableExecutor.
   *
   * When you need keep order for a series of requests, you can invoke {@link
   * HashableExecutor#submit(int, Runnable)} and pass a same hash number as the first argument of
   * those tasks, as result, those requests will be executed by submitting order.
   *
   * If you use the hash feature to keep order, the requests will be executed by a same thread. In
   * some cases, it could cause performance problem.
   */
  private HashableExecutor executor;

  public AbstractServer(ServerConfig serverConfig, Codec codec, Invoker invoker) {
    this.serverConfig = serverConfig;
    this.codec = codec;
    this.invoker = invoker;
    createExecutor();
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    return invoker.invoke(request, invocation);
  }

  @Override
  public Codec getCodec() {
    return codec;
  }

  @Override
  public HashableExecutor getExecutor() {
    return executor;
  }

  @Override
  public ServerConfig getConfig() {
    return serverConfig;
  }

  @Override
  public boolean isOpen() {
    return status == CONNECTED;
  }

  @Override
  public void open() {
    doOpen();
  }

  @Override
  public void close() {
    doClose();
    executor.shutdownGracefully();
  }

  protected abstract void doOpen();

  protected abstract void doClose();

  private void createExecutor() {
    int workerNum = serverConfig.getWorkerThreadNum() > 0 ? serverConfig.getWorkerThreadNum()
        : GlobalConstants.THREAD_NUMBER * 2;
    executor = new ConsistentHashLoopGroup(workerNum, HashableChooserFactory.INSTANCE);
  }

}
