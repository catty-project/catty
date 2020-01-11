package io.catty.example.pb;

import io.catty.example.pb.generated.EchoProtocol.Request;
import io.catty.example.pb.generated.EchoProtocol.Response;

public interface IService {

  Response echo(Request request);

}
