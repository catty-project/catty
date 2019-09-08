package com.nowcoder.remote;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public interface Future {

  boolean cancel();

  boolean isCancelled();

  boolean isDone();

  boolean isSuccess();

  Object getValue() throws InterruptedException;

  Exception getException();

  void addListener(FutureListener listener);

}
