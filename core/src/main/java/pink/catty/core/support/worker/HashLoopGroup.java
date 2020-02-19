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
package pink.catty.core.support.worker;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import io.netty.util.concurrent.EventExecutorChooserFactory.EventExecutorChooser;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * HashLoopGroup guarantees that the task from the same hash number will be dispatched to
 * the same thread.
 *
 * If you need to execute some task by order, you could use HashLoopGroup.
 */
public final class HashLoopGroup
    extends MultithreadEventExecutorGroup
    implements HashableExecutor {

  private static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Integer.MAX_VALUE;
  private static List<EventExecutor> children = new ArrayList<>(); // before <init>

  private HashableChooser chooser;
  // todo: add a classicExecutor.
  private ExecutorService classicExecutor;

  public HashLoopGroup(int threadNum, HashableChooserFactory chooserFactory) {
    this(threadNum, null, chooserFactory, DEFAULT_MAX_PENDING_EXECUTOR_TASKS,
        RejectedExecutionHandlers.reject());
    EventExecutorChooser chooser = chooserFactory
        .newChooser(children.toArray(new EventExecutor[0]));
    this.chooser = (HashableChooser) chooser;
  }

  private HashLoopGroup(int threadNum, Executor executor,
      EventExecutorChooserFactory chooserFactory, Object... args) {
    super(threadNum, executor, chooserFactory, args);
  }

  // EventExecutorGroup
  @Override
  protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
    EventExecutor newChildren = new DefaultEventExecutor(this, executor, (Integer) args[0],
        (RejectedExecutionHandler) args[1]);
    children.add(newChildren);
    return newChildren;
  }

  // HashableExecutor
  @Override
  public void submit(int hash, Runnable task) {
    chooser.next(hash).submit(task);
  }

}
