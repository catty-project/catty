package io.catty.meta;

import io.catty.core.CattyException;
import io.catty.core.DefaultResponse;
import io.catty.core.Invocation;
import io.catty.core.Invoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Response.ResponseStatus;
import io.catty.core.service.MethodMeta;
import io.catty.core.utils.ExceptionUtils;
import java.lang.reflect.InvocationTargetException;

public class ProviderInvoker implements Invoker {

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Response response = new DefaultResponse(request.getRequestId());
    String methodName = request.getMethodName();
    MethodMeta methodMeta = invocation.getInvokedMethod();

    if (methodMeta == null) {
      response.setStatus(ResponseStatus.OUTER_ERROR);
      response.setValue(ExceptionUtils
          .toString(new CattyException("ServiceInvoker: can't find method: " + methodName)));
      return response;
    }

    try {
      Object[] argsValue = request.getArgsValue();
      Object value = methodMeta.getMethod().invoke(invocation.getTarget(), argsValue);
      response.setStatus(ResponseStatus.OK);
      response.setValue(value);
    } catch (Exception e) {
      // todo: deal runtime exception.
      if(e instanceof InvocationTargetException) {
        // fixme: require jdk >= 1.4.
        Throwable targetException = e.getCause();
        if(methodMeta.containsCheckedException(targetException.getClass())) {
          // todo: EXCEPTED_ERROR => CHECKED_EXCEPTION.
          response.setStatus(ResponseStatus.EXCEPTED_ERROR);
          response.setValue(ExceptionUtils.toString(targetException));
        } else {
          response.setStatus(ResponseStatus.INNER_ERROR);
          response.setValue(ExceptionUtils.toString(e));
        }
      } else {
        response.setStatus(ResponseStatus.INNER_ERROR);
        response.setValue(ExceptionUtils.toString(e));
      }
    } catch (Error e) {
      response.setStatus(ResponseStatus.UNKNOWN_ERROR);
      response.setValue(ExceptionUtils.toString("Unknown Error!", e));
    }
    return response;
  }
}
