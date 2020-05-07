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
import pink.catty.core.service.MethodModel;
import pink.catty.core.service.NotSupportedMethodException;
import pink.catty.core.service.RpcMethod;
import pink.catty.core.service.ServiceModel;
import pink.catty.test.MethodModelTest.MockMethod.MockObject;

public class MethodModelTest {

  private static ServiceModel<MockMethod> serviceModel;

  @BeforeClass
  public static void init() {
    serviceModel = ServiceModel.parse(MockMethod.class);
  }

  @Test
  public void voidReturnTest() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("voidReturn");
    Assert.assertEquals(methodModel.getReturnType(), Void.TYPE);
    Assert.assertEquals(methodModel.getGenericReturnType(), Void.TYPE);
  }

  @Test
  public void primaryReturnTest() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("primaryReturn");
    Assert.assertEquals(methodModel.getReturnType(), Integer.TYPE);
    Assert.assertEquals(methodModel.getGenericReturnType(), Integer.TYPE);
  }

  @Test
  public void normalReturnTest() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("normalReturn");
    Assert.assertEquals(methodModel.getReturnType(), MockObject.class);
    Assert.assertEquals(methodModel.getGenericReturnType(), MockObject.class);
  }

  @Test
  public void listReturnTest() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("listReturn");
    Assert.assertEquals(methodModel.getReturnType(), List.class);
    Assert.assertEquals(methodModel.getGenericReturnType(), List.class);
  }

  @Test
  public void mapReturnTest() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("mapReturn");
    Assert.assertEquals(methodModel.getReturnType(), Map.class);
    Assert.assertEquals(methodModel.getGenericReturnType(), Map.class);
  }

  @Test
  public void futureReturn1Test() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("futureReturn1");
    Assert.assertEquals(methodModel.getReturnType(), CompletionStage.class);
    Assert.assertEquals(methodModel.getGenericReturnType(), List.class);
  }

  @Test
  public void futureReturn2Test() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("futureReturn2");
    Assert.assertEquals(methodModel.getReturnType(), CompletionStage.class);
    Assert.assertEquals(methodModel.getGenericReturnType(), MockObject[].class);
  }

  @Test
  public void genericReturnTest() {
    MethodModel methodModel = serviceModel.getMethodMetaByName("genericReturn");
    Assert.assertEquals(methodModel.getReturnType(), List.class);
    Assert.assertEquals(methodModel.getGenericReturnType(), List.class);
  }

  @Test(expected = NotSupportedMethodException.class)
  public void invalidMethod0Test() {
    ServiceModel.parse(InvalidMethod0.class);
  }

  @Test(expected = NotSupportedMethodException.class)
  public void invalidMethod1Test() {
    ServiceModel.parse(InvalidMethod1.class);
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
