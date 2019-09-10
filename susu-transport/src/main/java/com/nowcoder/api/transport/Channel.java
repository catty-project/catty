package com.nowcoder.api.transport;

import com.nowcoder.codec.Codec;
import com.nowcoder.config.RemoteConfig;

/**
 * 表示一个客户端与服务端的链接（或者说一个信道），用于管理这个链接的通用功能和生命周期。
 *
 * @author zrj CreateDate: 2019/9/8
 */
public interface Channel {

  /**
   * 使用在该信道上的编解码协议
   */
  Codec getCodec();

  /**
   * 该传输层的设置
   */
  RemoteConfig getConfig();

  boolean isOpened();

  boolean isClosed();

  /**
   * 打开信道
   */
  void open();

  /**
   * 关闭信道
   */
  void close();

}
