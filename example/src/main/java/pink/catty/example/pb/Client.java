package pink.catty.example.pb;

import pink.catty.config.Reference;
import pink.catty.config.ClientConfig;
import pink.catty.example.pb.generated.EchoProtocol;
import pink.catty.example.pb.generated.EchoProtocol.Request;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .addAddress("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    Request request = EchoProtocol.Request.newBuilder()
        .setValue("123321")
        .build();

    EchoProtocol.Response response = reference.refer().echo(request);
    System.out.println(response.getValue());
  }
}
