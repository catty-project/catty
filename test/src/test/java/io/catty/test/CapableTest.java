package io.catty.test;

import io.catty.test.service.EchoService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CapableTest extends BasicTest {

  @Test
  public void syncTest() {
    String echo = UUID.randomUUID().toString();
    EchoService service = reference.refer();
    String result = service.echo(echo);
    Assert.assertEquals(echo, result);
  }

  @Test
  public void asyncTest() {
    String echo = UUID.randomUUID().toString();
    EchoService service = reference.refer();
    CompletableFuture<String> future = service.asyncEcho(echo);
    try {
      Assert.assertEquals(echo, future.get());
    } catch (Exception e) {
      Assert.fail("error", e);
    }
  }

}
