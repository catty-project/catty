package io.catty.test.service;

import io.catty.core.utils.MD5Utils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestServiceImpl implements TestService {

  private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
      future.completeExceptionally(new Exception());
    });
    return future;
  }

  @Override
  public CompletableFuture<String> asyncException1(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      future.completeExceptionally(new Error());
    });
    return future;
  }

  @Override
  public CompletableFuture<String> asyncException2(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      future.completeExceptionally(new RuntimeException());
    });
    return future;
  }
}
