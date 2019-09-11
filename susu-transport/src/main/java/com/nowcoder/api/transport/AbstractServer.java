package com.nowcoder.api.transport;

import com.nowcoder.codec.Codec;
import com.nowcoder.core.URL;

/**
 * 服务端
 *
 * @author zrj CreateDate: 2019/9/8
 */
public class AbstractServer extends AbstractEndpoint implements Server {

  public AbstractServer(Codec codec, URL url) {
    super(codec, url);
  }
}
