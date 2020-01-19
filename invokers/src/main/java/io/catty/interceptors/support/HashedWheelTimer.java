package io.catty.interceptors.support;

public class HashedWheelTimer {

  private Table[] tables;

  public class Table {

    private Timeout head;


  }

  private class Timeout {
    private Timeout next;
    private Timeout pre;

    private Runnable timeoutCallback;

    private long timeout;
  }

}
