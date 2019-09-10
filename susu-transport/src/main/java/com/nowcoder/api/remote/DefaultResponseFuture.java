package com.nowcoder.api.remote;

/**
 * @author zrj CreateDate: 2019/9/5
 */
public class DefaultResponseFuture implements ResponseFuture {

  private static final int NEW = 0;
  private static final int SUCCESS = 1;
  private static final int CANCEL = 2;
  private static final int FAILED = 3;

  private final Object lock = new Object();

  private volatile int status;

  private Response value;


  public DefaultResponseFuture() {
    this.status = NEW;
  }

  @Override
  public void onSuccess(Response response) {
    synchronized (lock) {
      value = response;
      status = SUCCESS;
      lock.notifyAll();
    }
  }

  @Override
  public void onFailure(Response response) {
    synchronized (lock) {
      value = response;
      status = FAILED;
      lock.notifyAll();
    }
  }

  @Override
  public boolean cancel() {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public boolean isSuccess() {
    return false;
  }

  @Override
  public Object getValue() throws InterruptedException {
    if (status > 0) {
      return value;
    }
    synchronized (lock) {
      if (status > 0) {
        return value;
      }
      lock.wait();
    }
    return value;
  }

  @Override
  public Exception getException() {
    return null;
  }

  @Override
  public void addListener(FutureListener listener) {

  }
}
