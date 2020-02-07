package io.catty.example.registry;

import io.catty.config.Reference;
import io.catty.config.ClientConfig;
import io.catty.api.RegistryConfig;
import io.catty.example.IService;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:20550")
        .build();

    RegistryConfig registryConfig = new RegistryConfig();
    registryConfig.setAddress("127.0.0.1:2181");

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setRegistryConfig(registryConfig);
    reference.setInterfaceClass(IService.class);

    IService service = reference.refer();
    System.out.println(service.say0());
    System.out.println(service.say1("catty"));

  }

}
