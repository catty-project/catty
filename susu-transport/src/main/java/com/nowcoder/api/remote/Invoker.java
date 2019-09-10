package com.nowcoder.api.remote;

import com.nowcoder.core.LifeCircle;
import com.nowcoder.core.URL;

/**
 * @author zrj CreateDate: 2019/9/10
 */
public interface Invoker extends LifeCircle {

  default URL getURL() {
    return null;
  }

  Response invoke(Request request);

}
