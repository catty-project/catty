package codec.susu;

import codec.Serialization;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.IOException;

/**
 * @author zrj CreateDate: 2019/9/5
 */
public class FastJsonSerialization implements Serialization {

  @Override
  public byte[] serialize(Object object) throws IOException {
    SerializeWriter out = new SerializeWriter();
    JSONSerializer serializer = new JSONSerializer(out);
    serializer.config(SerializerFeature.WriteEnumUsingToString, true);
    serializer.config(SerializerFeature.WriteClassName, true);
    serializer.write(object);
    return out.toBytes("UTF-8");
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
    return JSON.parseObject(new String(bytes), clazz);
  }
}
