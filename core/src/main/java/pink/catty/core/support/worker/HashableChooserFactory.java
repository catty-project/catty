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

import io.netty.util.concurrent.DefaultEventExecutorChooserFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory;

/**
 * HashableChooserFactory is able to choose a more effective HashableChooser according to
 * executors' length. If length is power of two, a PowerOfTwoHashableChooser will be returned, or,
 * a GenericHashableChooser returned which use '%' to get mod.
 *
 * So, if you could, try an executors with power-of-two length will get better performance.
 *
 * @see DefaultEventExecutorChooserFactory
 */
public class HashableChooserFactory implements EventExecutorChooserFactory {

  public static final HashableChooserFactory INSTANCE = new HashableChooserFactory();

  private HashableChooserFactory() {
  }

  @Override
  public EventExecutorChooser newChooser(EventExecutor[] executors) {
    if (isPowerOfTwo(executors.length)) {
      return new PowerOfTwoHashableChooser(executors);
    } else {
      return new GenericHashableChooser(executors);
    }
  }

  private static boolean isPowerOfTwo(int val) {
    return (val & -val) == val;
  }

  private abstract static class AbstractHashableChooser implements HashableChooser {

    final EventExecutor[] executors;
    final int size;
    final EventExecutorChooser defaultChooser;

    AbstractHashableChooser(EventExecutor[] executors) {
      this.executors = executors;
      this.size = executors.length;
      this.defaultChooser = DefaultEventExecutorChooserFactory.INSTANCE
          .newChooser(executors);
    }

    @Override
    public EventExecutor next() {
      return defaultChooser.next();
    }
  }

  private static final class PowerOfTwoHashableChooser extends AbstractHashableChooser {

    public PowerOfTwoHashableChooser(EventExecutor[] executors) {
      super(executors);
    }

    @Override
    public EventExecutor next(int hash) {
      return executors[hash & size - 1];
    }
  }

  private static final class GenericHashableChooser extends AbstractHashableChooser {

    GenericHashableChooser(EventExecutor[] executors) {
      super(executors);
    }

    @Override
    public EventExecutor next(int hash) {
      return executors[hash % size];
    }
  }
}
