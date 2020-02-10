package pink.catty.invokers.linked;

import pink.catty.core.CattyException;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.Invoker;
import pink.catty.core.invoker.LinkedInvoker;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.utils.AsyncUtils;
import pink.catty.core.utils.ExceptionUtils;
import java.util.concurrent.CompletionStage;

public class ConsumerSerializationInvoker extends LinkedInvoker {

  private Serialization serialization;

  public ConsumerSerializationInvoker(Invoker next, Serialization serialization) {
    super(next);
    if (serialization == null) {
      throw new NullPointerException("Serialization is null");
    }
    this.serialization = serialization;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    assert invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.CONSUMER;
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
        return serialization.deserialize(data, invocation.getInvokedMethod().getReturnType());
      } else {
        return new Error("Unknown serialization head byte:" + bytes[0] + " except 0 or 1");
      }
    });
    return AsyncUtils.newResponse(newResponse, request.getRequestId());
  }

}
