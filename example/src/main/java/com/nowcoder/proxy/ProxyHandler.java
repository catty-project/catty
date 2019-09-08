package com.nowcoder.proxy;

import com.nowcoder.codec.susu.SusuCodec;
import com.nowcoder.remote.RemoteConfig;
import com.nowcoder.remote.Request;
import com.nowcoder.remote.Response;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import com.nowcoder.netty.NettyClient;


/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ProxyHandler implements InvocationHandler {

  private NettyClient client;

  public ProxyHandler() {
    this.client = new NettyClient(new SusuCodec(), new RemoteConfig(false));
    client.open();
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Request request = new Request();
    request.setInterfaceName(method.getDeclaringClass().getName());
    request.setMethodName(method.getName());
    request.setArgsType(getArgsTypeString(args));
    request.setArgsValue(args);
    Response response = client.request(request);

    if(response.getException() != null) {
      throw response.getException();
    }

    return response.getReturnValue();
  }

  private String getArgsTypeString(Object[] args) {
    if(args.length <= 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for(Object object : args) {
      sb.append(object.getClass().getName()).append(",");
    }
    if(sb.length() > 0) {
      sb.setLength(sb.length() - ",".length());
    }
    return sb.toString();
  }
}
