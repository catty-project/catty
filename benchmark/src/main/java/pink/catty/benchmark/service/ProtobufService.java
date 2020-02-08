package pink.catty.benchmark.service;

import pink.catty.benchmark.generated.BenchmarkProtocol.Response;
import pink.catty.benchmark.generated.BenchmarkProtocol.Request;

public interface ProtobufService {

  Response service(Request request);

}
