package org.fire.example.pb;

import org.fire.example.pb.generated.EchoProtocol.Request;
import org.fire.example.pb.generated.EchoProtocol.Response;

public class IServiceImpl implements IService {

  @Override
  public Response echo(Request request) {
    Response response = Response.newBuilder()
        .setValue(request.getValue())
        .build();

    return response;
  }
}
