package pink.catty.extension.serialization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.Serialization;

@Extension("FASTJSON")
public class FastJsonSerialization implements Serialization {

  @Override
  public byte[] serialize(Object object) {
    SerializeWriter out = new SerializeWriter();
    JSONSerializer serializer = new JSONSerializer(out);
    serializer.config(SerializerFeature.WriteEnumUsingToString, true);
    serializer.config(SerializerFeature.WriteClassName, true);
    serializer.write(object);
    return out.toBytes("UTF-8");
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    return JSON.parseObject(new String(bytes), clazz);
  }
}
