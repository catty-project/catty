package pink.catty.extension.codec;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import pink.catty.core.CodecException;
import pink.catty.core.extension.Extension;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.ProtobufPackageReader;
import pink.catty.core.invoker.DefaultRequest;
import pink.catty.core.invoker.DefaultResponse;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.extension.codec.generated.CattyProtocol;

@Extension("CATTY")
public class CattyCodec extends ProtobufPackageReader implements Codec {

  @Override
  public byte[] encode(Object message, DataTypeEnum dataTypeEnum) throws CodecException {
    byte[] body;
    if (dataTypeEnum == DataTypeEnum.REQUEST) {
      body = encodeRequestBody((Request) message);
    } else if (dataTypeEnum == DataTypeEnum.RESPONSE) {
      body = encodeResponseBody((Response) message);
    } else {
      throw new IllegalArgumentException("Unsupported encoder type.");
    }
    return encodeHeader(body);
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

  private byte[] encodeHeader(byte[] body) {
    int bodyLen = body.length;
    int headerLen = computeRawVarint32Size(bodyLen);
    byte[] data = new byte[bodyLen + headerLen];
    int pos = writeRawVarint32(data, bodyLen);
    System.arraycopy(body, 0, data, pos, bodyLen);
    return data;
  }

  private byte[] encodeRequestBody(Request request) {
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

  private byte[] encodeResponseBody(Response response) {
    return CattyProtocol.Response.newBuilder()
        .setRequestId(response.getRequestId())
        .setReturnValue(ByteString.copyFrom((byte[]) response.getValue()))
        .build()
        .toByteArray();
  }


  private int writeRawVarint32(byte[] data, int value) {
    for (int i = 0; i < data.length; i++) {
      if ((value & ~0x7F) == 0) {
        data[i] = (byte) value;
        return i + 1;
      } else {
        data[i] = (byte) ((value & 0x7F) | 0x80);
        value >>>= 7;
      }
    }
    throw new IllegalArgumentException("Encode varint error");
  }

  private int computeRawVarint32Size(final int value) {
    if ((value & (0xffffffff << 7)) == 0) {
      return 1;
    }
    if ((value & (0xffffffff << 14)) == 0) {
      return 2;
    }
    if ((value & (0xffffffff << 21)) == 0) {
      return 3;
    }
    if ((value & (0xffffffff << 28)) == 0) {
      return 4;
    }
    return 5;
  }
}
