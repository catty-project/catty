package io.catty.linked;

import io.catty.core.LinkedInvoker;
import io.catty.core.Invocation;
import io.catty.core.Invocation.InvokerLinkTypeEnum;
import io.catty.core.Invoker;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.Response.ResponseStatus;
import io.catty.core.extension.api.Serialization;
import io.catty.core.service.MethodMeta;
import java.util.concurrent.CompletionStage;

public class SerializationInvoker extends LinkedInvoker {

  private Serialization serialization;

  public SerializationInvoker(Invoker next, Serialization serialization) {
    super(next);
    if(serialization == null) {
      throw new NullPointerException("Serialization is null");
    }
    this.serialization = serialization;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.CONSUMER) {
      Object[] args = request.getArgsValue();
      if(args != null) {
        Object[] afterSerialize = new Object[args.length];
        for(int i = 0; i < args.length; i++) {
          afterSerialize[i] = serialization.serialize(args[i]);
        }
        request.setArgsValue(afterSerialize);
      }
    } else if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.PROVIDER) {
      Object[] args = request.getArgsValue();
      if(args != null) {
        MethodMeta methodMeta = invocation.getInvokedMethod();
        Class<?>[] parameterTypes = methodMeta.getMethod().getParameterTypes();
        Object[] afterDeserialize = new Object[args.length];
        for(int i = 0;i < args.length; i++) {
          if(args[i] instanceof byte[]) {
            afterDeserialize[i] = serialization.deserialize((byte[]) args[i], parameterTypes[i]);
          } else {
            afterDeserialize[i] = args[i];
          }
        }
        request.setArgsValue(afterDeserialize);
      }
    }

    Response response = next.invoke(request, invocation);

    MethodMeta methodMeta = invocation.getInvokedMethod();
    Object returnValue = response.getValue();
    if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.CONSUMER) {
      if(response.getStatus() != ResponseStatus.OK) {
        response.setValue(serialization.deserialize((byte[]) returnValue, String.class));
      } else {
        response.setValue(serialization.deserialize((byte[]) returnValue, methodMeta.getReturnType()));
      }
    } else if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.PROVIDER) {
      if(methodMeta.isAsync()) {
        CompletionStage originFuture = (CompletionStage) returnValue;
        response.setValue(originFuture.thenApply(serialization::serialize));
      } else {
        response.setValue(serialization.serialize(returnValue));
      }
    }
    return response;
  }

}
