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
package pink.catty.test.service.exception;

public class Test1CheckedException extends Exception {

  public Test1CheckedException() {
  }

  public Test1CheckedException(String message) {
    super(message);
  }

  public Test1CheckedException(String message, Throwable cause) {
    super(message, cause);
  }

  public Test1CheckedException(Throwable cause) {
    super(cause);
  }

  public Test1CheckedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
