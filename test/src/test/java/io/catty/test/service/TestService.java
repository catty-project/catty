package io.catty.test.service;

import java.util.concurrent.CompletableFuture;

public interface TestService {

  String echo(String name);

  CompletableFuture<String> asyncEcho(String name);

  String checkedException() throws Test1CheckedException;

  String multiCheckedException() throws Test1CheckedException, Test2CheckedException;

  /**
   * @throws NullPointerException
   */
  String runtimeException();

  /**
   * Completed with {@link Exception}
   */
  CompletableFuture<String> asyncException0(String name);

  /**
   * Completed with {@link Error}
   */
  CompletableFuture<String> asyncException1(String name);

  /**
   * Completed with {@link RuntimeException}
   */
  CompletableFuture<String> asyncException2(String name);
}
