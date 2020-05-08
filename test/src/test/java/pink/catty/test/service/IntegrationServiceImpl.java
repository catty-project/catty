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
package pink.catty.test.service;

import pink.catty.core.utils.MD5Utils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import pink.catty.test.service.exception.Test1CheckedException;
import pink.catty.test.service.exception.Test2CheckedException;

public class IntegrationServiceImpl implements IntegrationService {

  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  public void say0(String name) {
    // none
  }

  @Override
  public void say1(String name) {
    // none
  }

  @Override
  public String echo(String name) {
    return name;
  }

  @Override
  public CompletableFuture<String> asyncEcho(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
        future.complete(name);
      } catch (Exception e) {
        future.complete(MD5Utils.md5(name));
      }
    });
    return future;
  }

  @Override
  public String checkedException() throws Test1CheckedException {
    throw new Test1CheckedException("Test1CheckedException");
  }

  @Override
  public String multiCheckedException() throws Test1CheckedException, Test2CheckedException {
    throw new Test2CheckedException("Test2CheckedException");
  }

  @Override
  public String runtimeException() {
    throw new NullPointerException("NPE");
  }

  @Override
  public CompletableFuture<String> asyncException0(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      future.completeExceptionally(new Exception(name));
    });
    return future;
  }

  @Override
  public CompletableFuture<String> asyncException1(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      future.completeExceptionally(new Error(name));
    });
    return future;
  }

  @Override
  public CompletableFuture<String> asyncException2(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      future.completeExceptionally(new RuntimeException(name));
    });
    return future;
  }
}
