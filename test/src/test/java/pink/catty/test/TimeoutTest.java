/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.test;

import pink.catty.core.RpcTimeoutException;
import pink.catty.test.service.AnnotationService;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimeoutTest extends BasicTest {

  @Test
  public void testSyncTimeoutWouldNotHappen() {
    String echo = UUID.randomUUID().toString();
    AnnotationService service = annotationServiceReference.refer();
    String result = service.timeout0(echo);
    Assert.assertEquals(echo, result);
  }

  @Test
  public void testAsyncTimeoutWouldNotHappen() {
    String echo = UUID.randomUUID().toString();
    AnnotationService service = annotationServiceReference.refer();
    CompletableFuture<String> future = service.asyncTimeout0(echo);
    try {
      Assert.assertEquals(echo, future.get());
    } catch (Exception e) {
      Assert.fail("error", e);
    }
  }

  @Test(expectedExceptions = RpcTimeoutException.class)
  public void testSyncTimeoutWillHappen() {
    String echo = UUID.randomUUID().toString();
    AnnotationService service = annotationServiceReference.refer();
    String result = service.timeout1(echo);
    Assert.assertEquals(echo, result);
  }

  @Test(expectedExceptions = RpcTimeoutException.class)
  public void testAsyncTimeoutWillHappen() throws Throwable {
    String echo = UUID.randomUUID().toString();
    AnnotationService service = annotationServiceReference.refer();
    CompletableFuture<String> future = service.asyncTimeout1(echo);
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
