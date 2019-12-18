package org.fire.core.codec;


import org.fire.core.exception.CodecException;

public interface Codec {

  byte[] encode(Object message) throws CodecException;

  Object decode(byte[] data, DataTypeEnum dataTypeEnum) throws CodecException;

  enum DataTypeEnum {
    REQUEST,
    RESPONSE,
    ;
  }

}
