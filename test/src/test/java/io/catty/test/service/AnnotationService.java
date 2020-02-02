package io.catty.test.service;

import io.catty.core.service.Function;
import io.catty.core.service.Service;
import java.util.concurrent.CompletableFuture;

@Service(name = "AnnotationTest", version = "1.0.2", group = "test", timeout = 500)
public interface AnnotationService {

  /**
   * will not timeout.
   */
  @Function(timeout = 200)
  String timeout0(String name);

  /**
   * will not timeout.
   */
  @Function(timeout = 200)
  CompletableFuture<String> asyncTimeout0(String name);

  /**
   * Is going to timeout.
   */
  @Function(timeout = 50)
  String timeout1(String name);

  /**
   * Is going to timeout.
   */
  @Function(timeout = 200)
  CompletableFuture<String> asyncTimeout1(String name);

}
