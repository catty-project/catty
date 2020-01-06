package io.catty.api.worker;

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

/**
 * ConsistentHashLoopGroup guarantees that the task from the same hash number will be dispatched to
 * the same thread.
 *
 * If you need to execute some task by order, you could use ConsistentHashLoopGroup.
 */
public final class ConsistentHashLoopGroup
    extends MultithreadEventExecutorGroup
    implements HashableExecutor {

  private static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Integer.MAX_VALUE;

  private HashableChooser chooser;
  private static List<EventExecutor> children = new ArrayList<>(); // before <init>

  public ConsistentHashLoopGroup(int threadNum, HashableChooserFactory chooserFactory) {
    this(threadNum, null, chooserFactory, DEFAULT_MAX_PENDING_EXECUTOR_TASKS,
        RejectedExecutionHandlers.reject());
    EventExecutorChooser chooser = chooserFactory
        .newChooser(children.toArray(new EventExecutor[0]));
    this.chooser = (HashableChooser) chooser;
  }

  private ConsistentHashLoopGroup(int threadNum, Executor executor,
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
