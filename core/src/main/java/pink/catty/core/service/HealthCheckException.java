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
package pink.catty.core.service;

public class HealthCheckException extends RuntimeException {

  private Object invoker;

  public HealthCheckException(Object invoker) {
    this.invoker = invoker;
  }

  public HealthCheckException(String message, Object invoker) {
    super(message);
    this.invoker = invoker;
  }

  public HealthCheckException(String message, Throwable cause, Object invoker) {
    super(message, cause);
    this.invoker = invoker;
  }

  public HealthCheckException(Throwable cause, Object invoker) {
    super(cause);
    this.invoker = invoker;
  }

  public HealthCheckException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace, Object invoker) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.invoker = invoker;
  }

  public HealthCheckException() {
  }

  public HealthCheckException(String message) {
    super(message);
  }

  public HealthCheckException(String message, Throwable cause) {
    super(message, cause);
  }

  public HealthCheckException(Throwable cause) {
    super(cause);
  }

  public HealthCheckException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public Object getInvoker() {
    return invoker;
  }
}
