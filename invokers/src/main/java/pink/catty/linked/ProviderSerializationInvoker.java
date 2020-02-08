package pink.catty.linked;

import pink.catty.core.Invocation;
import pink.catty.core.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.Invoker;
import pink.catty.core.LinkedInvoker;
import pink.catty.core.Request;
import pink.catty.core.Response;
import pink.catty.core.extension.spi.Serialization;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.utils.AsyncUtils;
import pink.catty.core.utils.ExceptionUtils;
import java.util.concurrent.CompletionStage;

public class ProviderSerializationInvoker extends LinkedInvoker {

  private Serialization serialization;

  public ProviderSerializationInvoker(Invoker next, Serialization serialization) {
    super(next);
    if (serialization == null) {
      throw new NullPointerException("Serialization is null");
    }
    this.serialization = serialization;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    assert invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.PROVIDER;

    Object[] args = request.getArgsValue();
    if (args != null) {
      MethodMeta methodMeta = invocation.getInvokedMethod();
      Class<?>[] parameterTypes = methodMeta.getMethod().getParameterTypes();
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
    MethodMeta methodMeta = invocation.getInvokedMethod();
    CompletionStage<Object> newResponse = response.thenApply(returnValue -> {
      if(returnValue instanceof Throwable
          && !Throwable.class.isAssignableFrom(methodMeta.getReturnType()) ) {
        // exception has been thrown.
        String exception = ExceptionUtils.toString((Throwable) returnValue);
        byte[] serialized = serialization.serialize(exception);
        byte[] finalBytes = new byte[serialized.length + 1];
        finalBytes[0] = 1; // exception has been thrown.
        System.arraycopy(serialized, 0, finalBytes, 1, serialized.length);
        return finalBytes;
      } else {
        byte[] serialized = serialization.serialize(returnValue);
        byte[] finalBytes = new byte[serialized.length + 1];
        finalBytes[0] = 0; // response status is ok.
        System.arraycopy(serialized, 0, finalBytes, 1, serialized.length);
        return finalBytes;
      }
    });
    return AsyncUtils.newResponse(newResponse, request.getRequestId());
  }

}
