package io.catty.benchmark.service;

import io.catty.benchmark.generated.BenchmarkProtocol;
import io.catty.benchmark.generated.BenchmarkProtocol.Response;

public interface ProtobufService {

  Response service(BenchmarkProtocol.Request request);

}
