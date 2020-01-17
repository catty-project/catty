package io.catty.test;

import io.catty.CattyException;
import io.catty.test.service.Test1CheckedException;
import io.catty.test.service.Test2CheckedException;
import io.catty.test.service.TestService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CapableTest extends BasicTest {

  @Test
  public void multiRefer() {
    TestService service1 = reference.refer();
    TestService service2 = reference.refer();
    Assert.assertSame(service1, service2);
  }

  @Test
  public void syncTest() {
    String echo = UUID.randomUUID().toString();
    TestService service = reference.refer();
    String result = service.echo(echo);
    Assert.assertEquals(echo, result);
  }

  @Test
  public void asyncTest() {
    String echo = UUID.randomUUID().toString();
    TestService service = reference.refer();
    CompletableFuture<String> future = service.asyncEcho(echo);
    try {
      Assert.assertEquals(echo, future.get());
    } catch (Exception e) {
      Assert.fail("error", e);
    }
  }

  @Test(expectedExceptions = {Test1CheckedException.class})
  public void checkedExceptionTest() throws Test1CheckedException {
    TestService service = reference.refer();
    service.checkedException();
  }

  @Test(expectedExceptions = {Test1CheckedException.class, Test2CheckedException.class})
  public void checkedMultiExceptionTest() throws Test1CheckedException, Test2CheckedException {
    TestService service = reference.refer();
    service.multiCheckedException();
  }

  @Test(expectedExceptions = {CattyException.class})
  public void runtimeExceptionTest() {
    TestService service = reference.refer();
    service.runtimeException();
  }

}
