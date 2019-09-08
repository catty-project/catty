package com.nowcoder.codec;

import java.io.IOException;

/**
 * @author zrj CreateDate: 2019/9/5
 */
public interface Serialization {

  byte[] serialize(Object object) throws IOException;

  <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;

}
