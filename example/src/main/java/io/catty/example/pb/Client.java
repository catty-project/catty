package io.catty.example.pb;

import io.catty.config.Reference;
import io.catty.config.ClientConfig;
import io.catty.example.pb.generated.EchoProtocol;
import io.catty.example.pb.generated.EchoProtocol.Request;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:20550")
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
