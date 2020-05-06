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
package pink.catty.invokers.consumer;

import java.util.concurrent.CompletionStage;
import pink.catty.core.CattyException;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.invoker.AbstractConsumer;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.frame.DefaultRequest;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.utils.AsyncUtils;
import pink.catty.core.utils.ExceptionUtils;

public class ConsumerSerialization extends AbstractConsumer {

  private Serialization serialization;

  public ConsumerSerialization(Consumer next, Serialization serialization) {
    super(next);
    if (serialization == null) {
      throw new NullPointerException("Serialization is null");
    }
    this.serialization = serialization;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    assert invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.CONSUMER;
    request = new DefaultRequest(request.getRequestId(), request.getInterfaceName(),
        request.getMethodName(), request.getArgsValue());

    MethodMeta methodMeta = invocation.getInvokedMethod();
    Object[] args = request.getArgsValue();
    if (args != null) {
      Object[] afterSerialize = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
        afterSerialize[i] = serialization.serialize(args[i]);
      }
      request.setArgsValue(afterSerialize);
    }

    Response response = next.invoke(request, invocation);

    CompletionStage<Object> newResponse = response.thenApply(returnValue -> {
      if (!(returnValue instanceof byte[])) {
        return returnValue;
      }
      byte[] bytes = (byte[]) returnValue;
      byte[] data = new byte[bytes.length - 1];
      System.arraycopy(bytes, 1, data, 0, data.length);
      if (bytes[0] == 1) { // exception occurred.
        String exceptionString = serialization.deserialize(data, String.class);
        String[] exceptionInfo = ExceptionUtils.parseExceptionString(exceptionString);
        String exceptionClassName = exceptionInfo[0];
        String exceptionFullStack = exceptionInfo[1];
        if (methodMeta.containsCheckedException(exceptionClassName)) {
          Class<?> exceptionClass = methodMeta.getCheckedExceptionByName(exceptionClassName);
          return ExceptionUtils.getInstance(exceptionClass, exceptionFullStack);
        } else {
          try {
            Class<?> exceptionClass = Class.forName(exceptionClassName);
            return ExceptionUtils.getInstance(exceptionClass, exceptionFullStack);
          } catch (ClassNotFoundException e) {
            return new CattyException(exceptionFullStack);
          }
        }
      } else if (bytes[0] == 0) {
        if (methodMeta.isAsync()) {
          return serialization.deserialize(data, methodMeta.getGenericReturnType());
        } else {
          return serialization.deserialize(data, methodMeta.getReturnType());
        }
      } else {
        return new Error("Unknown serialization head byte:" + bytes[0] + " except 0 or 1");
      }
    });
    return AsyncUtils.newResponse(newResponse, request.getRequestId());
  }

}
