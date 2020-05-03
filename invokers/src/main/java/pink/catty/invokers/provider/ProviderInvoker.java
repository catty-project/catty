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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionStage;
import pink.catty.core.CattyException;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.Provider;
import pink.catty.core.invoker.frame.DefaultResponse;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ProviderMeta;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.utils.AsyncUtils;

public class ProviderInvoker implements Provider {

  private ProviderMeta meta;

  public ProviderInvoker(ProviderMeta meta) {
    this.meta = meta;
  }

  @Override
  public ProviderMeta getMeta() {
    return meta;
  }

  @Override
  public Invoker getNext() {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Response invoke(Request request, Invocation invocation) {
    Response response = new DefaultResponse(request.getRequestId());
    String methodName = request.getMethodName();
    MethodMeta methodMeta = invocation.getInvokedMethod();

    if (methodMeta == null) {
      response.setValue(new CattyException("ServiceInvoker: can't find method: " + methodName));
      return response;
    }

    try {
      Object[] argsValue = request.getArgsValue();
      Object value = methodMeta.getMethod().invoke(invocation.getTarget(), argsValue);
      if (methodMeta.isAsync()) {
        CompletionStage<Object> future = (CompletionStage<Object>) value;
        response = AsyncUtils.newResponse(future, request.getRequestId());
      } else {
        response.setValue(value);
      }
    } catch (Exception e) {
      // todo: deal runtime exception.
      if (e instanceof InvocationTargetException) {
        Throwable targetException = e.getCause();
        if (methodMeta.containsCheckedException(targetException.getClass())) {
          response.setValue(targetException);
        } else {
          response.setValue(e);
        }
      } else {
        response.setValue(e);
      }
    } catch (Error e) {
      response.setValue(e);
    }
    return response;
  }
}
