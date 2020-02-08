package pink.catty.example.pb;

import pink.catty.example.pb.generated.EchoProtocol.Request;
import pink.catty.example.pb.generated.EchoProtocol.Response;

public interface IService {

  Response echo(Request request);

}
