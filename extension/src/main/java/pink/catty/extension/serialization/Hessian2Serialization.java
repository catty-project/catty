package pink.catty.extension.serialization;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import pink.catty.core.SerializationException;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.Serialization;

@Extension("HESSIAN2")
public class Hessian2Serialization implements Serialization {

  @Override
  public byte[] serialize(Object object) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Hessian2Output out = new Hessian2Output(bos);
    try {
      out.writeObject(object);
      out.flush();
    } catch (IOException e) {
      throw new SerializationException("Hessian2 serialization error", e);
    }
    return bos.toByteArray();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
    try {
    return (T) input.readObject(clazz);
    } catch (IOException e) {
      throw new SerializationException("Hessian2 de-serialization error", e);
    }
  }
}
