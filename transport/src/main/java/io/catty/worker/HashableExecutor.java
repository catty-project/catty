package io.catty.worker;

import io.netty.util.concurrent.EventExecutorGroup;

/**
 * HashableExecutor is a extension of EventExecutorGroup.
 * HashableExecutor is able to dispatch tasks by hash number.
 * At the same time, HashableExecutor could be used as generic executor while it has
 * extended EventExecutorGroup.
 *
 * @see io.netty.util.concurrent.MultithreadEventExecutorGroup
 * @see ConsistentHashLoopGroup
 */
public interface HashableExecutor extends EventExecutorGroup {

  void submit(int hash, Runnable task);

}
