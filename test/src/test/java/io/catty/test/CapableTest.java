package io.catty.test;

import io.catty.core.CattyException;
import io.catty.test.service.Test1CheckedException;
import io.catty.test.service.Test2CheckedException;
import io.catty.test.service.TestService;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CapableTest extends BasicTest {

  @Test
  public void multiRefer() {
    TestService service1 = testServiceReference.refer();
    TestService service2 = testServiceReference.refer();
    Assert.assertSame(service1, service2);
  }

  @Test
  public void syncTest() {
    String echo = UUID.randomUUID().toString();
    TestService service = testServiceReference.refer();
    String result = service.echo(echo);
    Assert.assertEquals(echo, result);
  }

  @Test
  public void asyncTest() {
    String echo = UUID.randomUUID().toString();
    TestService service = testServiceReference.refer();
    CompletableFuture<String> future = service.asyncEcho(echo);
    try {
      Assert.assertEquals(echo, future.get());
    } catch (Exception e) {
      Assert.fail("error", e);
    }
  }

  @Test(expectedExceptions = {Test1CheckedException.class})
  public void checkedExceptionTest() throws Test1CheckedException {
    TestService service = testServiceReference.refer();
    service.checkedException();
  }

  @Test(expectedExceptions = {Test1CheckedException.class, Test2CheckedException.class})
  public void checkedMultiExceptionTest() throws Test1CheckedException, Test2CheckedException {
    TestService service = testServiceReference.refer();
    service.multiCheckedException();
  }

  @Test(expectedExceptions = {CattyException.class})
  public void runtimeExceptionTest() {
    TestService service = testServiceReference.refer();
    service.runtimeException();
  }

  @Test(expectedExceptions = {Exception.class})
  public void asyncExceptionTest() throws Throwable {
    TestService service = testServiceReference.refer();
    CompletableFuture<String> future = service.asyncException0("abc");
    testThrowable(future);
  }

  @Test(expectedExceptions = {Error.class})
  public void asyncErrorTest() throws Throwable {
    TestService service = testServiceReference.refer();
    CompletableFuture<String> future = service.asyncException1("abc");
    testThrowable(future);
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void asyncRuntimeExceptionTest() throws Throwable {
    TestService service = testServiceReference.refer();
    CompletableFuture<String> future = service.asyncException2("abc");
    testThrowable(future);
  }

  private void testThrowable(CompletableFuture<String> future) throws Throwable {
    List<Throwable> throwables = new LinkedList<>();
    future.whenComplete((value, throwable) -> {
      if (throwable != null) {
        throwables.add(throwable);
      }
    });
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      // ignore
    }
    Assert.assertEquals(throwables.size(), 1);
    throw throwables.get(0);
  }

}
