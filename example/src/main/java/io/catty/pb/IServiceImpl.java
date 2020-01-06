package io.catty.pb;

import io.catty.pb.generated.EchoProtocol.Request;
import io.catty.pb.generated.EchoProtocol.Response;

public class IServiceImpl implements IService {

  @Override
  public Response echo(Request request) {
    Response response = Response.newBuilder()
        .setValue(request.getValue())
        .build();

    return response;
  }
}
