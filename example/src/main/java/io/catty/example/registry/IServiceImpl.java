package io.catty.example.registry;

public class IServiceImpl implements IService {

  @Override
  public String say0() {
    return "from server";
  }

  @Override
  public String say1(String name) {
    return "from server " + name;
  }
}
