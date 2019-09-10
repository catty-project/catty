package com.nowcoder;

import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.lb.RandomLoadBalance;
import com.nowcoder.api.remote.Invoker;
import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zrj CreateDate: 2019/9/10
 */
public class DefaultCluster implements Cluster {

  private LoadBalance loadBalance;
  private List<Invoker> invokers;
  private List<URL> urls;
  private URL url;

  private volatile boolean init = false;

  /**
   * 没有传入urls，说明url是一个注册中心地址，需要去注册中心拿真正的server url，如果不是注册中心，代表这是为一个server
   */
//  public DefaultCluster(URL url) {
//    this.url = url;
//  }

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
    if("random".equals(url.getStringConfig(URL_CONFIG.LOAD_BALANCE))) {
      loadBalance = new RandomLoadBalance();
    } else {
      loadBalance = new RandomLoadBalance();
    }

    if(urls != null && urls.size() > 0) {
      invokers = urls.stream().map(this::getInvokerFromUrl).collect(Collectors.toList());
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
  public void notify(URL registryUrl, List<URL> urls) {

  }

  /**
   * 创建Invoker的逻辑先独立出来再说，之后可能会有很复杂的创建逻辑。
   */
  private Invoker getInvokerFromUrl(URL url) {
    return new DefaultInvoker(url);
  }
}


