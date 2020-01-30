package io.catty.benchmark.service;

import io.catty.core.utils.MD5Utils;

public class PojoServiceImpl implements PojoService {

  @Override
  public String service(String text) {
    return MD5Utils.md5(text);
  }
}
