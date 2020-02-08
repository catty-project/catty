package pink.catty.example.pb;

import pink.catty.example.pb.generated.EchoProtocol.Request;
import pink.catty.example.pb.generated.EchoProtocol.Response;

public class IServiceImpl implements IService {

  @Override
  public Response echo(Request request) {
    Response response = Response.newBuilder()
        .setValue(request.getValue())
        .build();

    return response;
  }
}
