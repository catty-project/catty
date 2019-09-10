package com.nowcoder.proxy;

import com.nowcoder.Cluster;
import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.core.URL;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ProxyHandler implements InvocationHandler {

  private Cluster cluster;
  private URL url;

  public ProxyHandler(URL url, Cluster cluster) {
    this.url = url;
    this.cluster = cluster;
    if(!this.cluster.isAvailable()) {
      this.cluster.init();
    }
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Request request = new Request();
    request.setInterfaceName(method.getDeclaringClass().getName());
    request.setMethodName(method.getName());
    request.setArgsType(getArgsTypeString(args));
    request.setArgsValue(args);
    Response response = cluster.invoke(request);

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
