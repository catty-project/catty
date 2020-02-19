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
