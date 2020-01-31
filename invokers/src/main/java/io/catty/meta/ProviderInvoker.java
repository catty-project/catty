package io.catty.meta;

import io.catty.core.CattyException;
import io.catty.core.DefaultResponse;
import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Response.ResponseEntity;
import io.catty.core.Response.ResponseStatus;
import io.catty.core.service.MethodMeta;
import io.catty.core.utils.AsyncUtils;
import io.catty.core.utils.ExceptionUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionStage;

public class ProviderInvoker implements Invoker {

  @SuppressWarnings("unchecked")
  @Override
  public Response invoke(Request request, Invocation invocation) {
    Response response = new DefaultResponse(request.getRequestId());
    String methodName = request.getMethodName();
    MethodMeta methodMeta = invocation.getInvokedMethod();

    if (methodMeta == null) {
      response.setResponseEntity(ResponseEntity.Of(ResponseStatus.OUTER_ERROR, ExceptionUtils
          .toString(new CattyException("ServiceInvoker: can't find method: " + methodName))));
      return response;
    }

    try {
      Object[] argsValue = request.getArgsValue();
      Object value = methodMeta.getMethod().invoke(invocation.getTarget(), argsValue);

      if (methodMeta.isAsync()) {
        CompletionStage<Object> future = (CompletionStage<Object>) value;
        response = AsyncUtils.newResponse(future, request.getRequestId());
      } else {
        response.setResponseEntity(ResponseEntity.Of(ResponseStatus.OK, value));
      }
    } catch (Exception e) {
      // todo: deal runtime exception.
      if (e instanceof InvocationTargetException) {
        // fixme: require jdk >= 1.4.
        Throwable targetException = e.getCause();
        if (methodMeta.containsCheckedException(targetException.getClass())) {
          // todo: EXCEPTED_ERROR => CHECKED_EXCEPTION.
          response.setResponseEntity(ResponseEntity
              .Of(ResponseStatus.EXCEPTED_ERROR, ExceptionUtils.toString(targetException)));
        } else {
          response.setResponseEntity(
              ResponseEntity.Of(ResponseStatus.INNER_ERROR, ExceptionUtils.toString(e)));
        }
      } else {
        response.setResponseEntity(
            ResponseEntity.Of(ResponseStatus.INNER_ERROR, ExceptionUtils.toString(e)));
      }
    } catch (Error e) {
      response.setResponseEntity(ResponseEntity
          .Of(ResponseStatus.UNKNOWN_ERROR, ExceptionUtils.toString("Unknown Error!", e)));
    }
    return response;
  }
}
