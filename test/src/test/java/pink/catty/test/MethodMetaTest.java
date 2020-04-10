/*
 * Copyright 2020 The Catty Project
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.service.NotSupportedMethodException;
import pink.catty.core.service.RpcMethod;
import pink.catty.core.service.ServiceMeta;
import pink.catty.test.MethodMetaTest.MockMethod.MockObject;

public class MethodMetaTest {

  private static ServiceMeta<MockMethod> serviceMeta;

  @BeforeClass
  public static void init() {
    serviceMeta = ServiceMeta.parse(MockMethod.class);
  }

  @Test
  public void voidReturnTest() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("voidReturn");
    Assert.assertEquals(methodMeta.getReturnType(), Void.TYPE);
    Assert.assertEquals(methodMeta.getGenericReturnType(), Void.TYPE);
  }

  @Test
  public void primaryReturnTest() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("primaryReturn");
    Assert.assertEquals(methodMeta.getReturnType(), Integer.TYPE);
    Assert.assertEquals(methodMeta.getGenericReturnType(), Integer.TYPE);
  }

  @Test
  public void normalReturnTest() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("normalReturn");
    Assert.assertEquals(methodMeta.getReturnType(), MockObject.class);
    Assert.assertEquals(methodMeta.getGenericReturnType(), MockObject.class);
  }

  @Test
  public void listReturnTest() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("listReturn");
    Assert.assertEquals(methodMeta.getReturnType(), List.class);
    Assert.assertEquals(methodMeta.getGenericReturnType(), List.class);
  }

  @Test
  public void mapReturnTest() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("mapReturn");
    Assert.assertEquals(methodMeta.getReturnType(), Map.class);
    Assert.assertEquals(methodMeta.getGenericReturnType(), Map.class);
  }

  @Test
  public void futureReturn1Test() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("futureReturn1");
    Assert.assertEquals(methodMeta.getReturnType(), CompletionStage.class);
    Assert.assertEquals(methodMeta.getGenericReturnType(), List.class);
  }

  @Test
  public void futureReturn2Test() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("futureReturn2");
    Assert.assertEquals(methodMeta.getReturnType(), CompletionStage.class);
    Assert.assertEquals(methodMeta.getGenericReturnType(), MockObject[].class);
  }

  @Test
  public void genericReturnTest() {
    MethodMeta methodMeta = serviceMeta.getMethodMetaByName("genericReturn");
    Assert.assertEquals(methodMeta.getReturnType(), List.class);
    Assert.assertEquals(methodMeta.getGenericReturnType(), List.class);
  }

  @Test(expected = NotSupportedMethodException.class)
  public void invalidMethod0Test() {
    ServiceMeta.parse(InvalidMethod0.class);
  }

  @Test(expected = NotSupportedMethodException.class)
  public void invalidMethod1Test() {
    ServiceMeta.parse(InvalidMethod1.class);
  }

  public interface MockMethod<T> {

    @RpcMethod(name = "voidReturn")
    void voidReturn();

    @RpcMethod(name = "primaryReturn")
    int primaryReturn();

    @RpcMethod(name = "normalReturn")
    MockObject normalReturn();

    @RpcMethod(name = "listReturn")
    List<MockObject> listReturn();

    @RpcMethod(name = "mapReturn")
    Map<MockObject, Integer> mapReturn();

    @RpcMethod(name = "futureReturn1")
    CompletionStage<List<T>> futureRetur1();

    @RpcMethod(name = "futureReturn2")
    CompletionStage<MockObject[]> futureRetur2();

    @RpcMethod(name = "genericReturn")
    List<T> genericReturn();

    class MockObject {

    }
  }

  public interface InvalidMethod0<T> {

    T foo();
  }

  public interface InvalidMethod1<T> {

    CompletionStage<T> foo();
  }

}
