package io.catty.example.sync;

import io.catty.config.Reference;
import io.catty.config.ClientConfig;
import io.catty.example.IService;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    IService service = reference.refer();
    System.out.println(service.say0());
    System.out.println(service.say1("catty"));
  }
}
