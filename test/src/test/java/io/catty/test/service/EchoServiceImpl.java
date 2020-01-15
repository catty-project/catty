package io.catty.test.service;

public class EchoServiceImpl implements EchoService {

  @Override
  public String echo(String name) {
    return name;
  }
}
