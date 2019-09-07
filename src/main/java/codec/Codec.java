package codec;

import exception.CodecException;

/**
 * 编解码器
 *
 * @author zrj CreateDate: 2019/9/5
 */
public interface Codec {

  byte[] encode(Object message) throws CodecException;

  Object decode(byte[] data) throws CodecException;

}
