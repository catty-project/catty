package pink.catty.test.service;

import pink.catty.core.service.RpcMethod;
import pink.catty.core.service.RpcService;
import java.util.concurrent.CompletableFuture;

@RpcService(name = "AnnotationTest", version = "1.0.2", group = "test", timeout = 500)
public interface AnnotationService {

  /**
   * will not timeout.
   */
  @RpcMethod(timeout = 200)
  String timeout0(String name);

  /**
   * will not timeout.
   */
  @RpcMethod(timeout = 200)
  CompletableFuture<String> asyncTimeout0(String name);

  /**
   * Is going to timeout.
   */
  @RpcMethod(timeout = 50)
  String timeout1(String name);

  /**
   * Is going to timeout.
   */
  @RpcMethod(timeout = 200)
  CompletableFuture<String> asyncTimeout1(String name);

}
