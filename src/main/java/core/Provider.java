package core;

import exception.SusuException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rpc.Request;
import transport.Handler;
import utils.ReflectUtils;

/**
 * @author zrj CreateDate: 2019/9/5
 */
public class Provider<T> implements Handler {

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
  public Object handle(Object message) {
    if(!(message instanceof Request)) {
      throw new SusuException("Provider: handle unsupported message type: " + message.getClass());
    }
    Request request = (Request) message;
    String methodName = ReflectUtils.getMethodDesc(request.getMethodName(), request.getArgsType());
    Method method = methodMap.get(methodName);
    if(method == null) {
      return new SusuException("Provider: can't find method: " + methodName);
    }
    try {
      return method.invoke(ref, request.getArgsValue());
    } catch (Exception e) {
      return new SusuException("Provider: exception when invoke method: " + methodName, e);
    } catch (Error e) {
      return new SusuException("Provider: error when invoke method: " + methodName, e);
    }
  }


}
