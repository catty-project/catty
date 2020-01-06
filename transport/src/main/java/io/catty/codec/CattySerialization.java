package io.catty.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.lang.reflect.Method;

public class CattySerialization implements Serialization {

  @Override
  public byte[] serialize(Object object) {
    byte[] result;
    if (object instanceof Builder) {
      object = ((Builder) object).build();
    }
    if (object instanceof Message) {
      result = ((Message) object).toByteArray();
    } else {
      result = backupSerialize(object);
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    Builder builder;
    try {
      Method method = clazz.getMethod("newBuilder");
      builder = (Builder) method.invoke(null, null);
    } catch (Exception e) {
      return backupDeserialize(bytes, clazz);
    }
    try {
      builder.mergeFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Deserialize error", e);
    }
    return (T) builder.build();
  }

  private byte[] backupSerialize(Object object) {
    SerializeWriter out = new SerializeWriter();
    JSONSerializer serializer = new JSONSerializer(out);
    serializer.config(SerializerFeature.WriteEnumUsingToString, true);
    serializer.config(SerializerFeature.WriteClassName, true);
    serializer.write(object);
    return out.toBytes("UTF-8");
  }

  private <T> T backupDeserialize(byte[] bytes, Class<T> clazz) {
    return JSON.parseObject(new String(bytes), clazz);
  }

}
