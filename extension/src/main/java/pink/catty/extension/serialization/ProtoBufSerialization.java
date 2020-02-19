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

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.lang.reflect.Method;
import pink.catty.core.SerializationException;
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
      throw new SerializationException(
          "Object's class: " + object.getClass() + " isn't instance of Builder or Message.");
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    Builder builder;
    try {
      Method method = clazz.getMethod("newBuilder");
      builder = (Builder) method.invoke(null, null);
    } catch (Exception e) {
      throw new SerializationException("Class: " + clazz + " couldn't find newBuilder method.");
    }
    try {
      builder.mergeFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new SerializationException("Deserialize error", e);
    }
    return (T) builder.build();
  }
}
