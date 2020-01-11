package io.catty;

import java.lang.Runtime;

public interface GlobalConstants {

  int THREAD_NUMBER = Runtime.getRuntime().availableProcessors();

  int DEFAULT_CLIENT_TIMEOUT = 3000; // 3 seconds.

}
