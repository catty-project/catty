package io.catty.example;

import java.util.concurrent.CompletableFuture;

public interface IService {

  String say0();

  String say1(String name);

  CompletableFuture<String> asyncSay(String name);

}
