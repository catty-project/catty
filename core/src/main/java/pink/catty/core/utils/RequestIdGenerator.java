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
package pink.catty.core.utils;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class RequestIdGenerator {

  private static final AtomicInteger curId = new AtomicInteger();
  private static final int MAX_PER_ROUND = 1 << 24;

  public static long next() {
    if (curId.longValue() >= MAX_PER_ROUND) {
      synchronized (RequestIdGenerator.class) {
        if (curId.get() >= MAX_PER_ROUND) {
          curId.set(0);
        }
      }
    }
    return curId.incrementAndGet();
  }
}
