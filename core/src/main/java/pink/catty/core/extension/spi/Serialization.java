package pink.catty.core.extension.spi;

public interface Serialization {

  byte[] serialize(Object object);

  <T> T deserialize(byte[] bytes, Class<T> clazz);

}
