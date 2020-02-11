package pink.catty.extension.serialization;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.lang.reflect.Method;
import pink.catty.core.CattyException;
import pink.catty.core.CodecException;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.Serialization;

@Extension("PROTOBUF")
public class ProtoBufSerialization implements Serialization {

  @Override
  public byte[] serialize(Object object) {
    byte[] result = null;
    if (object instanceof Builder) {
      object = ((Builder) object).build();
    }
    if (object instanceof Message) {
      result = ((Message) object).toByteArray();
    }
    if (result == null) {
      throw new CattyException(
          "Object's class: " + object.getClass() + " isn't instance of Builder or Message.");
    }
    return result;
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    Builder builder;
    try {
      Method method = clazz.getMethod("newBuilder");
      builder = (Builder) method.invoke(null, null);
    } catch (Exception e) {
      throw new CodecException("Class: " + clazz + " couldn't find newBuilder method.");
    }
    try {
      builder.mergeFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Deserialize error", e);
    }
    return (T) builder.build();
  }
}
