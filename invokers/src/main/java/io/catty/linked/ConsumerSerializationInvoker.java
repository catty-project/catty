package io.catty.linked;

import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.Invoker;
import io.catty.core.LinkedInvoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Response.ResponseEntity;
import io.catty.core.Response.ResponseStatus;
import io.catty.core.extension.api.Serialization;
import io.catty.core.utils.AsyncUtils;
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
      if (returnValue.getStatus() != ResponseStatus.OK) {
        return ResponseEntity.Of(returnValue.getStatus(),
            serialization.deserialize((byte[]) returnValue.getValue(), String.class));
      } else {
        return ResponseEntity.Of(returnValue.getStatus(), serialization
            .deserialize((byte[]) returnValue.getValue(),
                invocation.getInvokedMethod().getReturnType()));
      }
    });
    return AsyncUtils.newResponse(newResponse, request.getRequestId());
  }

}
