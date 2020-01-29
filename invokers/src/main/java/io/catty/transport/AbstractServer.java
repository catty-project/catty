package io.catty.transport;

import io.catty.GlobalConstants;
import io.catty.codec.Codec;
import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Server;
import io.catty.config.ServerConfig;
import io.catty.transport.worker.HashLoopGroup;
import io.catty.transport.worker.HashableChooserFactory;
import io.catty.transport.worker.HashableExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServer extends LinkedInvoker implements Server {

  private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);

  private static final int NEW = 0;
  private static final int CONNECTED = 1;
  private static final int DISCONNECTED = 2;

  private ServerConfig config;
  private volatile int status = NEW;
  private Codec codec;

  /**
   * HashableExecutor is needed because if every requests just be submitted randomly to a generic
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

  public AbstractServer(ServerConfig config, Codec codec, Invoker invoker) {
    super(invoker);
    this.config = config;
    this.codec = codec;
    createExecutor();
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    return next.invoke(request, invocation);
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
    return config;
  }

  @Override
  public boolean isAvailable() {
    return status == CONNECTED;
  }

  @Override
  public void init() {
    doOpen();
  }

  @Override
  public void destroy() {
    doClose();
    executor.shutdownGracefully();
  }

  protected abstract void doOpen();

  protected abstract void doClose();

  private void createExecutor() {
    int workerNum = config.getWorkerThreadNum() > 0 ? config.getWorkerThreadNum() :
        GlobalConstants.THREAD_NUMBER * 2;
    executor = new HashLoopGroup(workerNum, HashableChooserFactory.INSTANCE);
  }

}
