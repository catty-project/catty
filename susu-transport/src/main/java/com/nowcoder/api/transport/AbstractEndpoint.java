package com.nowcoder.api.transport;

import com.nowcoder.codec.Codec;
import com.nowcoder.core.URL;
import com.nowcoder.exception.CodecException;

/**
 * 一个可以通信的终端，需要有自己的编解码器：Codec。
 *
 * 需要有自己的设置：RemoteConfig
 *
 * 需要保存自己的状态：CHANNEL_STATUS
 *
 * @author zrj CreateDate: 2019/9/8
 */
abstract public class AbstractEndpoint implements Channel, Codec{

  private static final int UNKNOWN = -1;

  private static final int NEW = 0;

  private static final int OPEN = 1;

  private static final int CLOSE = 2;


  private Codec codec;

  private URL serverUrl;

  private volatile int CHANNEL_STATUS = 0;

  public AbstractEndpoint(Codec codec, URL serverUrl) {
    this.codec = codec;
    this.serverUrl = serverUrl;
  }

  @Override
  public Codec getCodec() {
    return codec;
  }

  @Override
  public URL getUrl() {
    return serverUrl;
  }

  public void setUrl(URL url) {
    this.serverUrl = url;
  }

  @Override
  public boolean isOpened() {
    return CHANNEL_STATUS == OPEN;
  }

  @Override
  public boolean isClosed() {
    return CHANNEL_STATUS == CLOSE;
  }

  @Override
  public void open() {
    CHANNEL_STATUS = OPEN;
  }

  @Override
  public void close() {
    CHANNEL_STATUS = CLOSE;
  }

  @Override
  public byte[] encode(Object message) throws CodecException {
    return codec.encode(message);
  }

  @Override
  public Object decode(byte[] data) throws CodecException {
    return codec.decode(data);
  }
}
