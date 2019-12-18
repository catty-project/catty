package org.fire.transport.codec;

import org.fire.core.codec.Codec;
import org.fire.core.codec.Serialization;
import org.fire.core.codec.generated.SusuProtocol;
import org.fire.core.exception.CodecException;
import org.fire.transport.api.DefaultResponse;
import org.fire.transport.api.ProtobufRequestDelegate;
import org.fire.transport.api.ProtobufResponseDelegate;

public class SusuCodec implements Codec {

  private Serialization serialization;

  public SusuCodec(Serialization serialization) {
    this.serialization = serialization;
  }

  @Override
  public byte[] encode(Object message) throws CodecException {
    if (message instanceof ProtobufRequestDelegate) {
      ProtobufRequestDelegate request = (ProtobufRequestDelegate) message;
      return request.getDelegatedRequest().toByteArray();
    }
    if (message instanceof ProtobufResponseDelegate) {
      ProtobufResponseDelegate response = (ProtobufResponseDelegate) message;
      return response.getDelegatedResponse().toByteArray();
    }
    if (message instanceof DefaultResponse) {
      ProtobufResponseDelegate response = new ProtobufResponseDelegate();
      DefaultResponse defaultResponse = (DefaultResponse) message;
      response.setRequestId(defaultResponse.getRequestId());
      response.setStatus(defaultResponse.getStatus());
      if (defaultResponse.getValue() != null) {
        response.setValue(defaultResponse.getValue());
      } else if (defaultResponse.getThrowable() != null) {
        response.setThrowable(defaultResponse.getThrowable());
      }
      response.build();
      return encode(response);
    }
    throw new IllegalArgumentException();
  }

  @Override
  public Object decode(byte[] data, DataTypeEnum dataTypeEnum) throws CodecException {
    try {
      if (DataTypeEnum.REQUEST == dataTypeEnum) {
        SusuProtocol.Request request = SusuProtocol.Request.parseFrom(data);
        return new ProtobufRequestDelegate(request);
      }
      if (DataTypeEnum.RESPONSE == dataTypeEnum) {
        SusuProtocol.Response response = SusuProtocol.Response.parseFrom(data);
        return new ProtobufResponseDelegate(response);
      }
      throw new CodecException("Illegal DataTypeEnum: " + dataTypeEnum);
    } catch (Exception e) {
      throw new CodecException("Decode error", e);
    }
  }
}
