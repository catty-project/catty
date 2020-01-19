package io.catty.interceptors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.catty.Invocation;
import io.catty.Invocation.InvokerLinkTypeEnum;
import io.catty.InvokerInterceptor;
import io.catty.Request;
import io.catty.Response;
import io.catty.Response.ResponseStatus;
import io.catty.codec.Serialization;
import io.catty.meta.service.MethodMeta;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class SerializationInterceptor implements Serialization, InvokerInterceptor {

  @Override
  public void beforeInvoke(Request request, Invocation invocation) {
    if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.CONSUMER) {
      Object[] args = request.getArgsValue();
      if(args != null) {
        Object[] afterSerialize = new Object[args.length];
        for(int i = 0; i < args.length; i++) {
          afterSerialize[i] = serialize(args[i]);
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
            afterDeserialize[i] = deserialize((byte[]) args[i], parameterTypes[i]);
          } else {
            afterDeserialize[i] = args[i];
          }
        }
        request.setArgsValue(afterDeserialize);
      }
    }
  }

  @Override
  public void afterInvoke(Response response, Invocation invocation) {
    MethodMeta methodMeta = invocation.getInvokedMethod();
    Object returnValue = response.getValue();
    if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.CONSUMER) {
      if(response.getStatus() != ResponseStatus.OK) {
        response.setValue(deserialize((byte[]) returnValue, String.class));
      } else {
        response.setValue(deserialize((byte[]) returnValue, methodMeta.getReturnType()));
      }
    } else if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.PROVIDER) {
      // fixme : or invocation.getMethod.isAsync is better?
      if(returnValue instanceof CompletableFuture) {
        CompletableFuture originFuture = (CompletableFuture) returnValue;
        response.setValue(originFuture.thenApply(this::serialize));
      } else {
        response.setValue(serialize(returnValue));
      }
    }
  }

  @Override
  public byte[] serialize(Object object) {
    byte[] result;
    if (object instanceof Builder) {
      object = ((Builder) object).build();
    }
    if (object instanceof Message) {
      result = ((Message) object).toByteArray();
    } else {
      SerializeWriter out = new SerializeWriter();
      JSONSerializer serializer = new JSONSerializer(out);
      serializer.config(SerializerFeature.WriteEnumUsingToString, true);
      serializer.config(SerializerFeature.WriteClassName, true);
      serializer.write(object);
      result = out.toBytes("UTF-8");
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    if(Message.class.isAssignableFrom(clazz)) {
      Builder builder;
      try {
        Method method = clazz.getMethod("newBuilder");
        builder = (Builder) method.invoke(null, null);
      } catch (Exception e) {
        throw new IllegalArgumentException("Deserialize error", e);
      }
      try {
        builder.mergeFrom(bytes);
      } catch (InvalidProtocolBufferException e) {
        throw new IllegalArgumentException("Deserialize error", e);
      }
      return (T) builder.build();
    } else {
      return JSON.parseObject(new String(bytes), clazz);
    }
  }

}
