/*
 *  Copyright 2009-2016 Weibo, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pink.catty.core.support.worker;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StandardThreadExecutor extends ThreadPoolExecutor {

  public static final int DEFAULT_MIN_THREADS = 20;
  public static final int DEFAULT_MAX_THREADS = 200;
  public static final int DEFAULT_MAX_IDLE_TIME = 60 * 1000; // 1 minutes

  protected AtomicInteger submittedTasksCount;
  private int maxSubmittedTaskCount;

  public StandardThreadExecutor() {
    this(DEFAULT_MIN_THREADS, DEFAULT_MAX_THREADS);
  }

  public StandardThreadExecutor(int coreThread, int maxThreads) {
    this(coreThread, maxThreads, maxThreads);
  }

  public StandardThreadExecutor(int coreThread, int maxThreads, long keepAliveTime, TimeUnit unit) {
    this(coreThread, maxThreads, keepAliveTime, unit, maxThreads);
  }

  public StandardThreadExecutor(int coreThreads, int maxThreads, int queueCapacity) {
    this(coreThreads, maxThreads, queueCapacity, Executors.defaultThreadFactory());
  }

  public StandardThreadExecutor(int coreThreads, int maxThreads, int queueCapacity,
      ThreadFactory threadFactory) {
    this(coreThreads, maxThreads, DEFAULT_MAX_IDLE_TIME, TimeUnit.MILLISECONDS, queueCapacity,
        threadFactory);
  }

  public StandardThreadExecutor(int coreThreads, int maxThreads, long keepAliveTime, TimeUnit unit,
      int queueCapacity) {
    this(coreThreads, maxThreads, keepAliveTime, unit, queueCapacity,
        Executors.defaultThreadFactory());
  }

  public StandardThreadExecutor(int coreThreads, int maxThreads, long keepAliveTime, TimeUnit unit,
      int queueCapacity, ThreadFactory threadFactory) {
    this(coreThreads, maxThreads, keepAliveTime, unit, queueCapacity, threadFactory,
        new AbortPolicy());
  }

  public StandardThreadExecutor(int coreThreads, int maxThreads, long keepAliveTime, TimeUnit unit,
      int queueCapacity, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(coreThreads, maxThreads, keepAliveTime, unit, new ExecutorQueue(), threadFactory,
        handler);
    ((ExecutorQueue) getQueue()).setStandardThreadExecutor(this);

    submittedTasksCount = new AtomicInteger(0);
    maxSubmittedTaskCount = queueCapacity + maxThreads;
  }

  public void execute(Runnable command) {
    int count = submittedTasksCount.incrementAndGet();

    if (count > maxSubmittedTaskCount) {
      submittedTasksCount.decrementAndGet();
      getRejectedExecutionHandler().rejectedExecution(command, this);
    }

    try {
      super.execute(command);
    } catch (RejectedExecutionException rx) {
      // there could have been contention around the queue
      if (!((ExecutorQueue) getQueue()).force(command)) {
        submittedTasksCount.decrementAndGet();

        getRejectedExecutionHandler().rejectedExecution(command, this);
      }
    }
  }

  public int getSubmittedTasksCount() {
    return this.submittedTasksCount.get();
  }

  public int getMaxSubmittedTaskCount() {
    return maxSubmittedTaskCount;
  }

  protected void afterExecute(Runnable r, Throwable t) {
    submittedTasksCount.decrementAndGet();
  }
}

class ExecutorQueue extends LinkedTransferQueue<Runnable> {

  private static final long serialVersionUID = -265236426751004839L;
  StandardThreadExecutor threadPoolExecutor;

  public ExecutorQueue() {
    super();
  }

  public void setStandardThreadExecutor(StandardThreadExecutor threadPoolExecutor) {
    this.threadPoolExecutor = threadPoolExecutor;
  }

  public boolean force(Runnable o) {
    if (threadPoolExecutor.isShutdown()) {
      throw new RejectedExecutionException(
          "Executor not running, can't force a command into the queue");
    }
    // forces the item onto the queue, to be used if the task is rejected
    return super.offer(o);
  }

  public boolean offer(Runnable o) {
    int poolSize = threadPoolExecutor.getPoolSize();

    // we are maxed out on threads, simply queue the object
    if (poolSize == threadPoolExecutor.getMaximumPoolSize()) {
      return super.offer(o);
    }
    // we have idle threads, just add it to the queue
    // note that we don't use getActiveCount(), see BZ 49730
    if (threadPoolExecutor.getSubmittedTasksCount() <= poolSize) {
      return super.offer(o);
    }
    // if we have less threads than maximum force creation of a new
    // thread
    if (poolSize < threadPoolExecutor.getMaximumPoolSize()) {
      return false;
    }
    // if we reached here, we need to add it to the queue
    return super.offer(o);
  }
}