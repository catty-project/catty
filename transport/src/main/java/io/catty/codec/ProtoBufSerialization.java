package io.catty.codec;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;
import io.catty.codec.Serialization;
import java.io.IOException;
import java.lang.reflect.Method;

public class ProtoBufSerialization implements Serialization {

  @Override
  public byte[] serialize(Object object) throws IOException {
    byte[] result;
    if (object instanceof Builder) {
      object = ((Builder) object).build();
    }
    if (object instanceof Message) {
      result = ((Message) object).toByteArray();
    } else {
      throw new IOException("Serialize error, only support protobuf serialization");
    }
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
    Builder builder;
    try {
      Method method = clazz.getMethod("newBuilder");
      builder = (Builder) method.invoke(null, null);
    } catch (Exception e) {
      throw new IOException("Deserialize error", e);
    }
    builder.mergeFrom(bytes);
    return (T) builder.build();
  }

}
