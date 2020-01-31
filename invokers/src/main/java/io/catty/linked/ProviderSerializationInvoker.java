package io.catty.linked;

import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Response.ResponseEntity;
import io.catty.core.extension.api.Serialization;
import io.catty.core.service.MethodMeta;
import io.catty.core.utils.AsyncUtils;
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
    CompletionStage<Object> newResponse = response.thenApply(returnValue -> ResponseEntity
        .Of(returnValue.getStatus(), serialization.serialize(returnValue.getValue())));
    return AsyncUtils.newResponse(newResponse, request.getRequestId());
  }

}
