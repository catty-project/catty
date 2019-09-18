package com.nowcoder.router;

import com.nowcoder.Invoker;
import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.exception.SusuException;
import com.nowcoder.utils.ReflectUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zrj CreateDate: 2019/9/5
 */
public class Provider<T> implements Invoker {

  protected Map<String, Method> methodMap = new ConcurrentHashMap<>();

  private T ref;

  private Class<T> interfaceClazz;

  /**
   * 找到所有interfaceClazz可以调用的方法，并缓存下来，缓存名字要保留参数类型的完整名称，防止函数重载
   */
  public Provider(T ref, Class<T> interfaceClazz) {
    if(!interfaceClazz.isInterface()) {
      throw new SusuException("Provider: interfaceClazz is not a interface!");
    }
    this.ref = ref;
    this.interfaceClazz = interfaceClazz;
    List<Method> methods = ReflectUtils.parseMethod(interfaceClazz);
    for(Method method : methods) {
      String methodDesc = ReflectUtils.getMethodDesc(method);
      methodMap.putIfAbsent(methodDesc, method);
    }
  }

  @Override
  public Response invoke(Request request) {
    Response response = new Response();
    response.setRequestId(request.getRequestId());
    String methodName = ReflectUtils.getMethodDesc(request.getMethodName(), request.getArgsType());
    Method method = methodMap.get(methodName);
    if(method == null) {
      response.setException(new SusuException("Provider: can't find method: " + methodName));
      return response;
    }
    try {
      Object value =  method.invoke(ref, request.getArgsValue());
      response.setReturnValue(value);
    } catch (Exception e) {
      response.setException(new SusuException("Provider: com.nowcoder.exception when invoke method: " + methodName, e));
    } catch (Error e) {
      response.setException(new SusuException("Provider: error when invoke method: " + methodName, e));
    }
    return response;
  }

  @Override
  public void init() {

  }

  @Override
  public void destroy() {

  }

  @Override
  public boolean isAvailable() {
    return false;
  }
}
