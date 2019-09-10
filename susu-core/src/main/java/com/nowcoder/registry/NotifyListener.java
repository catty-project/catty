package com.nowcoder.registry;

import com.nowcoder.core.URL;
import java.util.List;

/**
 * @author zrj CreateDate: 2019/9/9
 */
public interface NotifyListener {

  void notify(URL registryUrl, List<URL> urls);

}
