package io.catty;

import io.catty.Response.ResponseStatus;
import io.catty.meta.service.MethodMeta;
import io.catty.utils.ExceptionUtils;
import java.lang.reflect.InvocationTargetException;

public class ProviderInvoker implements Invoker {

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Response response = new DefaultResponse();
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
      response.setValue(value);
      response.setStatus(ResponseStatus.OK);
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
