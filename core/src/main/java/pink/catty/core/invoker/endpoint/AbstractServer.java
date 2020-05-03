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
package pink.catty.core.invoker.endpoint;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import pink.catty.core.CattyException;
import pink.catty.core.Constants;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.Provider;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ServerMeta;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.service.ServiceMeta;
import pink.catty.core.support.worker.HashLoopGroup;
import pink.catty.core.support.worker.HashableChooserFactory;
import pink.catty.core.support.worker.HashableExecutor;
import pink.catty.core.support.worker.StandardThreadExecutor;

public abstract class AbstractServer extends AbstractEndpoint implements Server {

  private ServerMeta serverMeta;
  private ExecutorService executor;
  private volatile Map<String, Provider> invokerMap = new ConcurrentHashMap<>();

  public AbstractServer(ServerMeta serverMeta, Codec codec) {
    super(codec);
    this.serverMeta = serverMeta;
    createExecutor();
  }

  @Override
  public void setInvokerMap(Map<String, Provider> invokerMap) {
    this.invokerMap = invokerMap;
  }

  @Override
  public void registerInvoker(String serviceIdentify, Provider provider) {
    invokerMap.put(serviceIdentify, provider);
  }

  @Override
  public Provider unregisterInvoker(String serviceIdentify) {
    return invokerMap.remove(serviceIdentify);
  }

  @Override
  public Provider getInvoker(String invokerIdentify) {
    return invokerMap.get(invokerIdentify);
  }

  @Override
  public ServerMeta getMeta() {
    return serverMeta;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    String serviceName = request.getInterfaceName();
    Provider provider = getInvoker(serviceName);
    if (provider == null) {
      throw new CattyException(
          "No such provider found! RpcService name: " + request.getInterfaceName());
    }

    if (invocation == null) {
      invocation = new Invocation(InvokerLinkTypeEnum.PROVIDER);
    }
    ServiceMeta serviceMeta = provider
        .getMeta()
        .getServiceMeta();
    MethodMeta methodMeta = provider
        .getMeta()
        .getServiceMeta()
        .getMethodMetaByName(request.getMethodName());
    invocation.setTarget(serviceMeta.getTarget());
    invocation.setServiceMeta(serviceMeta);
    invocation.setInvokedMethod(methodMeta);
    return provider.invoke(request, invocation);
  }

  @Override
  public ExecutorService getExecutor() {
    return executor;
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
    if (serverMeta.isNeedOrder()) {
      int workerNum = serverMeta.getWorkerThreadNum() > 0 ? serverMeta.getWorkerThreadNum() :
          Constants.THREAD_NUMBER * 2;
      executor = new HashLoopGroup(workerNum, HashableChooserFactory.INSTANCE);
    } else {
      int minWorkerNum = serverMeta.getMinWorkerThreadNum() > 0 ? serverMeta.getMinWorkerThreadNum() :
          Constants.THREAD_NUMBER * 2;
      int maxWorkerNum = serverMeta.getMaxWorkerThreadNum() > 0 ? serverMeta.getMaxWorkerThreadNum() :
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
    return Objects.equals(serverMeta, that.serverMeta);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverMeta);
  }
}
