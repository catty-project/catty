package pink.catty.example.async;

import pink.catty.config.Reference;
import pink.catty.config.ClientConfig;
import pink.catty.example.IService;
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
