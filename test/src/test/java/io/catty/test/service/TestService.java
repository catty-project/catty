package io.catty.test.service;

import java.util.concurrent.CompletableFuture;

public interface TestService {

  String echo(String name);

  CompletableFuture<String> asyncEcho(String name);

  String checkedException() throws Test1CheckedException;

  String multiCheckedException() throws Test1CheckedException, Test2CheckedException;
}
