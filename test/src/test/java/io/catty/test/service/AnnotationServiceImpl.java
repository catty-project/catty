package io.catty.test.service;

import io.catty.core.utils.MD5Utils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AnnotationServiceImpl implements AnnotationService {

  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  public String timeout0(String name) {
    return name;
  }

  @Override
  public CompletableFuture<String> asyncTimeout0(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> future.complete(name));
    return future;
  }

  @Override
  public String timeout1(String name) {
    try {
      TimeUnit.MILLISECONDS.sleep(300);
    } catch (Exception e) {
      // ignore
    }
    return name;
  }

  @Override
  public CompletableFuture<String> asyncTimeout1(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(300);
        future.complete(name);
      } catch (Exception e) {
        future.complete(MD5Utils.md5(name));
      }
    });
    return future;
  }

}
