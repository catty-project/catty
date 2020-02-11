package pink.catty.example.registry;

import pink.catty.config.Reference;
import pink.catty.config.ClientConfig;
import pink.catty.registry.api.RegistryConfig;
import pink.catty.example.IService;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
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
