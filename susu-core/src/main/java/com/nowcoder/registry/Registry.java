package com.nowcoder.registry;

import com.nowcoder.core.LifeCircle;
import com.nowcoder.core.URL;
import java.util.List;

/**
 * @author zrj CreateDate: 2019/9/9
 */
public interface Registry extends LifeCircle {

  void register(URL url);

  void unregister(URL url);

  void subscribe(URL url, NotifyListener listener);

  void unsubscribe(URL url, NotifyListener listener);

  List<URL> discover(URL url);

}
