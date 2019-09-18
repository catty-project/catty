package com.nowcoder;

import com.nowcoder.api.transport.Handler;
import com.nowcoder.core.LifeCircle;
import com.nowcoder.core.URL;
import com.nowcoder.router.Provider;

/**
 * @author zrj CreateDate: 2019/9/17
 */
public interface Router extends Handler, LifeCircle {

  void addNewProvider(URL url, Provider provider);

}
