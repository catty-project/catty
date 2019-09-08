package com.nowcoder.common;

import com.nowcoder.exception.CodecException;


/**
 * @author zrj CreateDate: 2019/9/5
 */
public interface CodecConstants {

  short MAGIC_HEAD = 9527;

  int HEADER_LENGTH = 8 * 16;

  int HEADER_SIZE = 16;

  enum Version {
    VERSION_1((byte) 0x01),

    ;

    private byte value;

    public byte getValue() {
      return value;
    }

    Version(byte value) {
      this.value = value;
    }

    public static Version getVersionByValue(byte value) throws CodecException {
      for(Version version : Version.values()) {
        if(version.value == value) {
          return version;
        }
      }
      throw new CodecException("Unknown version value: " + value);
    }
  }

  enum DataType {
    VOID((byte) 0x00),
    REQUEST((byte) 0x01),
    RESPONSE((byte) 0x02),
    EXCEPTION((byte) 0x03),


    ;

    private byte value;

    public byte getValue() {
      return value;
    }

    DataType(byte value) {
      this.value = value;
    }

    public static DataType getDataTypeByValue(byte value) throws CodecException {
      for(DataType dataType : DataType.values()) {
        if(dataType.value == value) {
          return dataType;
        }
      }
      throw new CodecException("Unknown type value: " + value);
    }
  }

}
