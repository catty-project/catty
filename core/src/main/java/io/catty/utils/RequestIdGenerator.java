package io.catty.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestIdGenerator {

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
