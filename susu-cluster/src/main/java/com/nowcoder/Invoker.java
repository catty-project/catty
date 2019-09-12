package com.nowcoder;

import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.core.LifeCircle;
import com.nowcoder.core.URL;

/**
 * invoker抽象了一个Client到一个Server的连接，可以认为是上层的Channel
 *
 * @author zrj CreateDate: 2019/9/10
 */
public interface Invoker extends LifeCircle {

  default URL getURL() {
    return null;
  }

  Response invoke(Request request);

}
