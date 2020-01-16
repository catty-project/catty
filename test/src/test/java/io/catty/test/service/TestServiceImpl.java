package io.catty.test.service;

import io.catty.utils.MD5Utils;
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
}
