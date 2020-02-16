package pink.catty.core;

import java.lang.Runtime;

public interface GlobalConstants {

  int THREAD_NUMBER = Runtime.getRuntime().availableProcessors();

  int DEFAULT_CLIENT_TIMEOUT = 3000; // 3 seconds.

  String HEARTBEAT_SERVICE_NAME = "cd6a8da1-0271-4e71-91a6-9bf1f9fcc212";

  String HEARTBEAT_METHOD_NAME = "1074fb58-0b58-4b15-87cb-de33082c4f51";

}
