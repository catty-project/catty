package pink.catty.example.sync;

import pink.catty.config.Reference;
import pink.catty.config.ClientConfig;
import pink.catty.core.extension.ExtensionType.SerializationType;
import pink.catty.example.IService;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setSerializationType(SerializationType.HESSIAN2);
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    IService service = reference.refer();
    System.out.println(service.say0());
    System.out.println(service.say1("catty"));
  }
}
