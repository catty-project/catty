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
package pink.catty.core;

import java.lang.Runtime;

public interface Constants {

  int THREAD_NUMBER = Runtime.getRuntime().availableProcessors();

  int DEFAULT_CLIENT_TIMEOUT = 3000; // 3 seconds.

  String HEARTBEAT_SERVICE_NAME = "cd6a8da1-0271-4e71-91a6-9bf1f9fcc212";

  String HEARTBEAT_METHOD_NAME = "1074fb58-0b58-4b15-87cb-de33082c4f51";

}
