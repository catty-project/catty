package io.catty.core;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Response extends CompletionStage<Object>, Future<Object> {

  long getRequestId();

  Object getValue();

  void setValue(Object value);

  void await() throws InterruptedException, ExecutionException;

  void await(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException;

}
