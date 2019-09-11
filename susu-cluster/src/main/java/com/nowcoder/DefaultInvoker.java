package com.nowcoder;

import com.nowcoder.codec.Codec;
import com.nowcoder.susu.SusuCodec;
import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.netty.NettyClient;
import com.nowcoder.api.remote.Invoker;
import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.api.transport.Client;

/**
 * @author zrj CreateDate: 2019/9/10
 */
public class DefaultInvoker implements Invoker {

  private Client client;
  private URL url;

  public DefaultInvoker(URL url) {
    this.url = url;
    Codec codec;
    // 检测传输层和编码层各用什么样的实现，如果没有使用默认实现代替
    if("netty".equals(url.getString(URL_CONFIG.TRANSPORT))) {
      if("susu".equals(url.getString(URL_CONFIG.CODEC))) {
        codec = new SusuCodec();
      } else {
        codec = new SusuCodec();
      }
      client = new NettyClient(codec, url);
    } else {
      client = new NettyClient(new SusuCodec(), url);
    }
  }

  @Override
  public Response invoke(Request request) {
    // 先进行最简单的实现
    return client.invoke(request);
  }

  @Override
  public URL getURL() {
    return url;
  }

  @Override
  public void init() {
    client.open();
  }

  @Override
  public void destroy() {
    client.close();
  }

  @Override
  public boolean isAvailable() {
    return client.isOpened();
  }
}
