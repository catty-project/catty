package pink.catty.invokers.meta;

import pink.catty.core.CattyException;
import pink.catty.core.invoker.DefaultResponse;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.utils.AsyncUtils;
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
