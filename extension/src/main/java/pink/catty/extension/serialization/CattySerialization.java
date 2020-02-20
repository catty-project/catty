/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.extension.serialization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.lang.reflect.Method;
import pink.catty.core.SerializationException;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionType.SerializationType;
import pink.catty.core.extension.spi.Serialization;

@Extension(SerializationType.PROTOBUF_FASTJSON)
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
    Builder builder;
    try {
      Method method = clazz.getMethod("newBuilder");
      builder = (Builder) method.invoke(null, null);
    } catch (Exception e) {
      return JSON.parseObject(new String(bytes), clazz);
    }
    try {
      builder.mergeFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new SerializationException("Deserialize error", e);
    }
    return (T) builder.build();
  }
}
