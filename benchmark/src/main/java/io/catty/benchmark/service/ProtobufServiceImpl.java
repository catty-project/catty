package io.catty.benchmark.service;

import io.catty.benchmark.generated.BenchmarkProtocol;
import io.catty.utils.MD5Utils;

public class ProtobufServiceImpl implements ProtobufService {

  @Override
  public BenchmarkProtocol.Response service(
      BenchmarkProtocol.Request request) {
    return BenchmarkProtocol.Response.newBuilder()
        .setValue(MD5Utils.md5(request.getValue()))
        .build();
  }
}
