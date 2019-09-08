package com.nowcoder.remote;

import com.nowcoder.codec.Codec;

/**
 * @author zrj CreateDate: 2019/9/8
 */
public interface Channel {

  Codec getCodec();

  RemoteConfig getConfig();

  boolean isOpened();

  boolean isClosed();

  void open();

  void close();

}
