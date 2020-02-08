package io.catty.example.async;

import io.catty.config.Reference;
import io.catty.config.ClientConfig;
import io.catty.example.IService;
import java.util.concurrent.CompletableFuture;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    IService service = reference.refer();
    CompletableFuture<String> future = service.asyncSay("catty");
    future.whenComplete((value, t) -> {
      System.out.println(value);
    });
  }
}
