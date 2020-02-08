package pink.catty.benchmark.service;

import pink.catty.benchmark.generated.BenchmarkProtocol;
import pink.catty.core.utils.MD5Utils;
import pink.catty.benchmark.generated.BenchmarkProtocol.Request;
import pink.catty.benchmark.generated.BenchmarkProtocol.Response;

public class ProtobufServiceImpl implements ProtobufService {

  @Override
  public Response service(
      Request request) {
    return BenchmarkProtocol.Response.newBuilder()
        .setValue(MD5Utils.md5(request.getValue()))
        .build();
  }
}
