package com.nowcoder;

import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.lb.RandomLoadBalance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zrj CreateDate: 2019/9/10
 */
public class DefaultCluster implements Cluster {

  private LoadBalance loadBalance;
  private List<Invoker> invokers;
  private List<URL> urls;
  private URL url;

  private Map<URL, Invoker> invokerMap = new ConcurrentHashMap<>();

  private volatile boolean init = false;

  /**
   * 没有传入urls, 会去registry中拿地址
   */
  public DefaultCluster(URL url) {
    this.url = url;
  }

  public DefaultCluster(URL url, List<URL> urls) {
    this.urls = urls;
    this.url = url;
  }

  @Override
  public Response invoke(Request request) {
    if(!init) {
      throw new SusuException("DefaultCluster: status error when invoke(), init: " + init);
    }
    if(invokers == null || invokers.size() == 0) {
      throw new SusuException("DefaultCluster: invokers is empty");
    }
    Invoker invoker = loadBalance.select(invokers);
    if(!invoker.isAvailable()) {
      invoker.init();
    }
    return invoker.invoke(request);
  }

  @Override
  public void init() {
    // 初始化负载均衡策略
    if("random".equals(url.getString(URL_CONFIG.LOAD_BALANCE))) {
      loadBalance = new RandomLoadBalance();
    } else {
      loadBalance = new RandomLoadBalance();
    }

    // 根据urls构建invoker
    if(urls != null && urls.size() > 0) {
      for(URL url : urls) {
        Invoker invoker = getInvokerFromUrl(url);
        invokers.add(invoker);
        invokerMap.put(url, invoker);
      }
    }
    init = true;
  }

  @Override
  public void destroy() {
    for(Invoker invoker : invokers) {
      if(invoker.isAvailable()) {
        invoker.destroy();
      }
    }
    init = false;
  }

  @Override
  public boolean isAvailable() {
    return init;
  }

  @Override
  public synchronized void notify(URL registryUrl, List<URL> urls) {
    this.urls = urls;
    if(invokers != null) {
      Map<URL, Invoker> newMap = new ConcurrentHashMap<>();
      List<URL> oldStillAvailable = new ArrayList<>();
      for(Entry<URL, Invoker> entry : invokerMap.entrySet()) {
        URL url = entry.getKey();
        if(urls.contains(url)) {
          // 继续存在的服务
          oldStillAvailable.add(url);
          newMap.put(entry.getKey(), entry.getValue());
        } else {
          // 销毁已经不存在的服务
          entry.getValue().destroy();
        }
      }

      // 找到新增的服务
      urls.removeAll(oldStillAvailable);
      for(URL url : urls) {
        newMap.put(url, getInvokerFromUrl(url));
      }
      // 替换新的服务列表
      invokerMap = newMap;
      invokers = new ArrayList<>(newMap.values());
    }
  }

  /**
   * 创建Invoker的逻辑先独立出来再说，之后可能会有很复杂的创建逻辑。
   */
  private Invoker getInvokerFromUrl(URL url) {
    return new DefaultInvoker(url);
  }
}


