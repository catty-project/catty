package org.fire.example.pb;

import org.fire.cluster.Reference;
import org.fire.core.config.ClientConfig;
import org.fire.example.pb.generated.EchoProtocol;

public class Client {

  public static void main(String[] args) {
    ClientConfig clientConfig = ClientConfig.builder()
        .address("127.0.0.1:20550")
        .build();

    Reference<IService> reference = new Reference<>();
    reference.setClientConfig(clientConfig);
    reference.setInterfaceClass(IService.class);

    EchoProtocol.Request request = EchoProtocol.Request.newBuilder()
        .setValue("123321")
        .build();

    EchoProtocol.Response response = reference.refer().echo(request);
    System.out.println(response.getValue());
  }
}
