package pink.catty.test;

import pink.catty.core.support.timer.HashedWheelTimer;
import pink.catty.core.support.timer.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimerTest {

  private Timer timer = new HashedWheelTimer();

  @Test
  public void testTimer0() {
    CompletableFuture future = new CompletableFuture();
    Object obj = new Object();
    timer.newTimeout(timeout ->
        future.complete(obj), 200, TimeUnit.MILLISECONDS);
    try {
      Assert.assertSame(future.get(), obj);
    } catch (Exception e) {
      Assert.fail("fail", e);
    }
  }

  @Test
  public void testTimer1() {
    CompletableFuture future = new CompletableFuture();
    Object obj = new Object();
    timer.newTimeout(timeout ->
        future.complete(obj), 0, TimeUnit.MILLISECONDS);
    try {
      Assert.assertSame(future.get(), obj);
    } catch (Exception e) {
      Assert.fail("fail", e);
    }
  }

}
