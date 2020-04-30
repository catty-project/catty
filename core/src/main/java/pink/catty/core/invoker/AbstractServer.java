/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.core.invoker;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import pink.catty.core.Constants;
import pink.catty.core.config.InnerServerConfig;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.support.worker.HashLoopGroup;
import pink.catty.core.support.worker.HashableChooserFactory;
import pink.catty.core.support.worker.HashableExecutor;
import pink.catty.core.support.worker.StandardThreadExecutor;

public abstract class AbstractServer extends AbstractEndpoint implements Server, LinkedInvoker {

  private InnerServerConfig config;
  protected Invoker next;


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
  private ExecutorService executor;

  public AbstractServer(InnerServerConfig config, Codec codec, MappedInvoker invoker) {
    super(codec);
    this.config = config;
    setNext(invoker);
    createExecutor();
  }

  @Override
  public void setNext(Invoker next) {
    this.next = next;
  }

  @Override
  public Invoker getNext() {
    return next;
  }

  @Override
  public InvokerRegistry getInvokerRegistry() {
    return (InvokerRegistry) next;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    return next.invoke(request, invocation);
  }

  @Override
  public ExecutorService getExecutor() {
    return executor;
  }

  @Override
  public InnerServerConfig getConfig() {
    return config;
  }

  @Override
  public void close() {
    super.close();
    if (executor instanceof HashableExecutor) {
      ((HashableExecutor) executor).shutdownGracefully();
    } else {
      executor.shutdown();
    }
  }

  protected abstract void doOpen();

  protected abstract void doClose();

  private void createExecutor() {
    if (config.isNeedOrder()) {
      int workerNum = config.getWorkerThreadNum() > 0 ? config.getWorkerThreadNum() :
          Constants.THREAD_NUMBER * 2;
      executor = new HashLoopGroup(workerNum, HashableChooserFactory.INSTANCE);
    } else {
      int minWorkerNum = config.getMinWorkerThreadNum() > 0 ? config.getMinWorkerThreadNum() :
          Constants.THREAD_NUMBER * 2;
      int maxWorkerNum = config.getMaxWorkerThreadNum() > 0 ? config.getMaxWorkerThreadNum() :
          Constants.THREAD_NUMBER * 4;
      executor = new StandardThreadExecutor(minWorkerNum, maxWorkerNum);
      ((StandardThreadExecutor) executor).prestartAllCoreThreads();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractServer that = (AbstractServer) o;
    return Objects.equals(config, that.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(config);
  }
}
