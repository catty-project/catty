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
import pink.catty.core.invoker.endpoint.Empty;
import pink.catty.core.invoker.frame.DefaultRequest;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.service.MethodModel;
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
  public Response invoke(Request request) {
    request = new DefaultRequest(request.getRequestId(),
        request.getInterfaceName(),
        request.getMethodName(),
        request.getArgsValue(),
        request.getServiceModel(),
        request.getInvokedMethod(),
        request.getTarget()
    );

    Object[] args = request.getArgsValue();
    if (args != null) {
      Object[] afterSerialize = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
        afterSerialize[i] = serialization.serialize(args[i]);
      }
      request.setArgsValue(afterSerialize);
    }

    Response response = next.invoke(request);

    MethodModel methodModel = request.getInvokedMethod();
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
        if (methodModel.containsCheckedException(exceptionClassName)) {
          Class<?> exceptionClass = methodModel.getCheckedExceptionByName(exceptionClassName);
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
        Object result;
        if (methodModel.isAsync()) {
          result = serialization.deserialize(data, methodModel.getGenericReturnType());
        } else {
          if (methodModel.isNeedReturn() && methodModel.getReturnType() == Void.TYPE) {
            result = serialization.deserialize(data, Empty.class);
          } else {
            result = serialization.deserialize(data, methodModel.getReturnType());
          }
        }
        return result;
      } else {
        return new Error("Unknown serialization head byte:" + bytes[0] + " except 0 or 1");
      }
    });
    return AsyncUtils.newResponse(newResponse, request.getRequestId());
  }

}
