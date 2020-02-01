package io.catty.linked;

import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.extension.api.Serialization;
import io.catty.core.service.MethodMeta;
import io.catty.core.utils.AsyncUtils;
import io.catty.core.utils.ExceptionUtils;
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
