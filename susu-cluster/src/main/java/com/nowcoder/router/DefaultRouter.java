package com.nowcoder.router;

import com.nowcoder.Router;
import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.api.transport.Server;
import com.nowcoder.common.constants.SusuConstants;
import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.netty.NettyServer;
import com.nowcoder.susu.SusuCodec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zrj CreateDate: 2019/9/17
 */
public class DefaultRouter implements Router {

  private Map<String, Provider> providerMap = new ConcurrentHashMap<>();

  private Server server;

  private URL url;

  public DefaultRouter(URL url) {
    this.url = url;
    this.server = new NettyServer(new SusuCodec(), url, this);
  }

  @Override
  public Object handle(Object message) {
    if (!(message instanceof Request)) {
      throw new SusuException(""/* todo: */);
    }
    Request request = (Request) message;
    Provider provider = chooseProvider(getServerKey(url));
    Response response = provider.invoke(request);
    return response;
  }

  @Override
  public void addNewProvider(URL url, Provider provider) {
    providerMap.put(getServerKey(url), provider);
  }

  @Override
  public void init() {
    server.open();
  }

  @Override
  public void destroy() {
    server.close();
  }

  @Override
  public boolean isAvailable() {
    return server.isOpened();
  }

  private String getServerKey(URL url) {
    return url.getString(URL_CONFIG.GROUP) + SusuConstants.PATH_SEP
        + url.getPath() + SusuConstants.PATH_SEP
        + url.getString(URL_CONFIG.VERSION);
  }

  private Provider chooseProvider(String serverKey) {
    return providerMap.get(serverKey);
  }

}
