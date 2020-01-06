package io.catty.codec;

import com.google.protobuf.ByteString;
import io.catty.Request;
import io.catty.Response;
import io.catty.Response.ResponseStatus;
import io.catty.api.DefaultRequest;
import io.catty.api.DefaultResponse;
import io.catty.codec.generated.CattyProtocol;
import io.catty.exception.CodecException;
import java.util.ArrayList;

public class CattyCodec implements Codec {

  @Override
  public byte[] encode(Object message, DataTypeEnum dataTypeEnum) throws CodecException {
    if (dataTypeEnum == DataTypeEnum.REQUEST) {
      Request request = (Request) message;
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
    }
    if (dataTypeEnum == DataTypeEnum.RESPONSE) {
      Response response = (Response) message;
      return CattyProtocol.Response.newBuilder()
          .setRequestId(response.getRequestId())
          .setStatus(response.getStatus().toString())
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
        return new DefaultResponse(response.getRequestId(),
            ResponseStatus.valueOf(response.getStatus()), response.getReturnValue().toByteArray());
      }
      throw new CodecException("Illegal DataTypeEnum: " + dataTypeEnum);
    } catch (Exception e) {
      throw new CodecException("Decode error", e);
    }
  }
}
