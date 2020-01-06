package io.catty.codec;


import io.catty.exception.CodecException;

public interface Codec {

  byte[] encode(Object message, DataTypeEnum dataTypeEnum) throws CodecException;

  Object decode(byte[] data, DataTypeEnum dataTypeEnum) throws CodecException;

  enum DataTypeEnum {
    REQUEST,
    RESPONSE,
    ;
  }

}
