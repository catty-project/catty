package io.catty.core.extension.api;

public interface Serialization {

  byte[] serialize(Object object);

  <T> T deserialize(byte[] bytes, Class<T> clazz);

}
