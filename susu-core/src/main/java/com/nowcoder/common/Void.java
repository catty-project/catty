package com.nowcoder.common;

/**
 * @author zrj CreateDate: 2019/9/5
 */
public class Void {

  private Void(){}

  public static Void getInstance() {
    return InstanceHolder.aVoid;
  }

  private static class InstanceHolder {
    private static Void aVoid = new Void();
  }

}
