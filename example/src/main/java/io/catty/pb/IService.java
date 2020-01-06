package io.catty.pb;

import io.catty.pb.generated.EchoProtocol.Request;
import io.catty.pb.generated.EchoProtocol.Response;

public interface IService {

  Response echo(Request request);

}
