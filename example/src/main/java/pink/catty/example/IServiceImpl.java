package pink.catty.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IServiceImpl implements IService {

  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  public String say0() {
    return "from server";
  }

  @Override
  public String say1(String name) {
    return "from server " + name;
  }

  @Override
  public CompletableFuture<String> asyncSay(String name) {
    CompletableFuture<String> future = new CompletableFuture<>();
    executorService.submit(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(500);
        future.complete("async from server " + name);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    return future;
  }

}
