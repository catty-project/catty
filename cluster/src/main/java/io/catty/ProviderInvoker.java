package io.catty;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.catty.codec.generated.SusuProtocol.Status;
import io.catty.exception.SusuException;
import io.catty.utils.ReflectUtils;
import io.catty.api.DefaultResponse;


public class ProviderInvoker<T> implements Invoker<T> {

  protected Map<String, Method> methodMap = new ConcurrentHashMap<>();

  private T ref;

  private Class<T> interfaceClazz;

  /**
   * cache all interface's methods
   */
  public ProviderInvoker(T ref, Class<T> interfaceClazz) {
    if (!interfaceClazz.isInterface()) {
      throw new SusuException("ProviderInvoker: interfaceClazz is not a interface!");
    }
    this.ref = ref;
    this.interfaceClazz = interfaceClazz;
    List<Method> methods = ReflectUtils.parseMethod(interfaceClazz);
    for (Method method : methods) {
      String methodDesc = ReflectUtils.getMethodDesc(method);
      String methodName = method.getName();
      methodMap.putIfAbsent(methodDesc, method);
      if (methodMap.containsKey(methodName)) {
        throw new SusuException(
            "Duplicated method name: " + method.getDeclaringClass() + "#" + method
                + ". Method name excepted unique.");
      }
      methodMap.put(method.getName(), method);
    }
  }

  @Override
  public Class<T> getInterface() {
    return interfaceClazz;
  }

  @Override
  public Response invoke(Request request) {
    Response response = new DefaultResponse();
    String methodName = request.getMethodName();
    Method method = methodMap.get(methodName);
    if (method == null) {
      response.setStatus(Status.OUTER_ERROR);
      response.setThrowable(new SusuException("ProviderInvoker: can't find method: " + methodName));
      return response;
    }
    try {
      Object[] argsValue = resolveArgsValue(request.getArgsValue(), method);
      Object value = method.invoke(ref, argsValue);
      if (value == null) {
        response.setValue(Void.TYPE);
      } else {
        response.setValue(value);
      }
      response.setStatus(Status.OK);
    } catch (Exception e) {
      response.setStatus(Status.INNER_ERROR);
      response.setThrowable(
          new SusuException("ProviderInvoker: exception when invoke method: " + methodName, e));
    } catch (Error e) {
      response.setStatus(Status.INNER_ERROR);
      response
          .setThrowable(
              new SusuException("ProviderInvoker: error when invoke method: " + methodName, e));
    }
    response.build();
    return response;
  }

  @SuppressWarnings("unchecked")
  private Object[] resolveArgsValue(Object[] args, Method method) throws IOException {
    if (args == null) {
      return null;
    }
    Object[] resolvedArgs = new Object[args.length];
    Class<?>[] parameterTypes = method.getParameterTypes();
    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof Any && Message.class.isAssignableFrom(parameterTypes[i])) {
        resolvedArgs[i] = ((Any) args[i]).unpack((Class<Message>) parameterTypes[i]);
      } else {
        resolvedArgs[i] = args[i];
      }
    }
    return resolvedArgs;
  }

}
