package pink.catty.extension.codec;

import com.google.protobuf.ByteString;
import pink.catty.core.CodecException;
import pink.catty.core.invoker.DefaultRequest;
import pink.catty.core.invoker.DefaultResponse;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.codec.generated.CattyProtocol;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.Codec;
import java.util.ArrayList;

@Extension("CATTY")
public class CattyCodec implements Codec {

  @Override
  public byte[] encode(Object message, DataTypeEnum dataTypeEnum) throws CodecException {
    if (dataTypeEnum == DataTypeEnum.REQUEST) {
      Request request = (Request) message;
      if (request.getArgsValue() != null) {
        ArrayList<ByteString> values = new ArrayList<>(request.getArgsValue().length);
        for (int i = 0; i < request.getArgsValue().length; i++) {
          values.add(ByteString.copyFrom((byte[]) request.getArgsValue()[i]));
        }
        return CattyProtocol.Request.newBuilder()
            .setRequestId(request.getRequestId())
            .addAllArguments(values)
            .setInterfaceName(request.getInterfaceName())
            .setMethodName(request.getMethodName())
            .build()
            .toByteArray();
      } else {
        return CattyProtocol.Request.newBuilder()
            .setRequestId(request.getRequestId())
            .setInterfaceName(request.getInterfaceName())
            .setMethodName(request.getMethodName())
            .build()
            .toByteArray();
      }
    }
    if (dataTypeEnum == DataTypeEnum.RESPONSE) {
      Response response = (Response) message;
      return CattyProtocol.Response.newBuilder()
          .setRequestId(response.getRequestId())
          .setReturnValue(ByteString.copyFrom((byte[]) response.getValue()))
          .build()
          .toByteArray();
    }
    throw new IllegalArgumentException();
  }

  @Override
  public Object decode(byte[] data, DataTypeEnum dataTypeEnum) throws CodecException {
    try {
      if (DataTypeEnum.REQUEST == dataTypeEnum) {
        CattyProtocol.Request request = CattyProtocol.Request.parseFrom(data);
        Object[] args = new Object[request.getArgumentsCount()];
        for (int i = 0; i < request.getArgumentsCount(); i++) {
          args[i] = request.getArguments(i).toByteArray();
        }
        return new DefaultRequest(request.getRequestId(), request.getInterfaceName(),
            request.getMethodName(), args);
      }
      if (DataTypeEnum.RESPONSE == dataTypeEnum) {
        CattyProtocol.Response response = CattyProtocol.Response.parseFrom(data);
        Response response0 = new DefaultResponse(response.getRequestId());
        response0.setValue(response.getReturnValue().toByteArray());
        return response0;
      }
      throw new CodecException("Illegal DataTypeEnum: " + dataTypeEnum);
    } catch (Exception e) {
      throw new CodecException("Decode error", e);
    }
  }

}
