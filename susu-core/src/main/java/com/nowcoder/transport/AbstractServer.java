package com.nowcoder.transport;

import com.nowcoder.codec.Codec;
import com.nowcoder.remote.RemoteConfig;

/**
 * @author zrj CreateDate: 2019/9/8
 */
public class AbstractServer extends AbstractEndpoint implements Server {

  public AbstractServer(Codec codec, RemoteConfig remoteConfig) {
    super(codec, remoteConfig);
  }
}
