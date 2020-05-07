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
package pink.catty.invokers.provider;

import java.util.concurrent.CompletionStage;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.invoker.AbstractProvider;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Provider;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.service.MethodModel;
import pink.catty.core.utils.AsyncUtils;
import pink.catty.core.utils.ExceptionUtils;

public class ProviderSerialization extends AbstractProvider {

  private Serialization serialization;

  public ProviderSerialization(Provider next, Serialization serialization) {
    super(next);
    if (serialization == null) {
      throw new NullPointerException("Serialization is null");
    }
    this.serialization = serialization;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Object[] args = request.getArgsValue();
    if (args != null) {
      MethodModel methodModel = invocation.getInvokedMethod();
      Class<?>[] parameterTypes = methodModel.getMethod().getParameterTypes();
      Object[] afterDeserialize = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof byte[]) {
          afterDeserialize[i] = serialization.deserialize((byte[]) args[i], parameterTypes[i]);
        } else {
          afterDeserialize[i] = args[i];
        }
      }
      request.setArgsValue(afterDeserialize);
    }

    Response response = next.invoke(request, invocation);
    MethodModel methodModel = invocation.getInvokedMethod();
    Class<?> returnType =
        methodModel.isAsync() ? methodModel.getGenericReturnType() : methodModel.getReturnType();

    CompletionStage<Object> newResponse = response.thenApply(returnValue -> {
      byte[] serialized;
      byte[] finalBytes;
      if (returnValue instanceof Throwable && !Throwable.class.isAssignableFrom(returnType)) {
        // exception has been thrown.
        String exception = ExceptionUtils.toString((Throwable) returnValue);
        serialized = serialization.serialize(exception);
        finalBytes = new byte[serialized.length + 1];
        finalBytes[0] = 1; // exception has been thrown.
        System.arraycopy(serialized, 0, finalBytes, 1, serialized.length);
      } else {
        try {
          serialized = serialization.serialize(returnValue);
          finalBytes = new byte[serialized.length + 1];
          finalBytes[0] = 0; // response status is ok.
        } catch (Exception e) {
          String exception = ExceptionUtils.toString(e);
          serialized = serialization.serialize(exception);
          finalBytes = new byte[serialized.length + 1];
          finalBytes[0] = 1; // exception has been thrown.
        }
        System.arraycopy(serialized, 0, finalBytes, 1, serialized.length);
      }
      return finalBytes;
    });
    return AsyncUtils.newResponse(newResponse, request.getRequestId());
  }

}
