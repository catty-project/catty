package pink.catty.example.extension;

import pink.catty.config.ClientConfig;
import pink.catty.config.Reference;
import pink.catty.core.extension.ExtensionFactory;
import pink.catty.example.IService;

public class Client {

  private static final String MY_SLB = "MyLittleCatty";

  public static void main(String[] args) {
    ExtensionFactory.getLoadBalance().register(MY_SLB, MyLoadBalance.class);

    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .addAddress("127.0.0.1:20551")
        .addAddress("127.0.0.1:20552")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);
    reference.setLoadbalanceType(MY_SLB);

    IService service = reference.refer();
    System.out.println(service.say0());
    System.out.println(service.say1("catty"));
  }
}
