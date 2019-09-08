package com.nowcoder.transport;

import com.nowcoder.codec.Codec;
import com.nowcoder.exception.CodecException;
import com.nowcoder.remote.Channel;
import com.nowcoder.remote.RemoteConfig;

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

  private RemoteConfig remoteConfig;

  private volatile int CHANNEL_STATUS = 0;

  public AbstractEndpoint(Codec codec, RemoteConfig remoteConfig) {
    this.codec = codec;
    this.remoteConfig = remoteConfig;
  }

  @Override
  public Codec getCodec() {
    return codec;
  }

  @Override
  public RemoteConfig getConfig() {
    return remoteConfig;
  }

  public void setConfig(RemoteConfig remoteConfig) {
    this.remoteConfig = remoteConfig;
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
