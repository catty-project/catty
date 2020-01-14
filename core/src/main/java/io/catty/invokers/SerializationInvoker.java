package io.catty.invokers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.catty.Invocation;
import io.catty.Invocation.InvokerLinkTypeEnum;
import io.catty.Invoker;
import io.catty.Request;
import io.catty.Response;
import io.catty.codec.Serialization;
import java.lang.reflect.Method;

public class SerializationInvoker implements Serialization, Invoker {

  private Invoker invoker;

  public SerializationInvoker(Invoker invoker) {
    this.invoker = invoker;
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.CONSUMER) {

    } else if(invocation.getLinkTypeEnum() == InvokerLinkTypeEnum.PROVIDER) {

    }

    return null;
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
