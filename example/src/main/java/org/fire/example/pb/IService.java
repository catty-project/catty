package org.fire.example.pb;

import org.fire.example.pb.generated.EchoProtocol;

public interface IService {

  EchoProtocol.Response echo(EchoProtocol.Request request);

}
