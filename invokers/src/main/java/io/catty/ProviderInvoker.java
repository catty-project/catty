package io.catty;

import io.catty.Response.ResponseStatus;
import io.catty.codec.Serialization;
import io.catty.meta.service.MethodMeta;
import io.catty.utils.ExceptionUtils;
import io.catty.utils.ReflectUtils;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ProviderInvoker<T> implements Invoker {

  private Map<String, Method> methodCache = new ConcurrentHashMap<>();
  private Map<Method, Set<Class<?>>> methodExceptionsCache = new ConcurrentHashMap<>();

  private T ref;

  /**
   * cache all interface's methods
   */
  public ProviderInvoker(T ref, Class<T> interfaceClazz, Serialization serialization) {
    if (!interfaceClazz.isInterface()) {
      throw new CattyException("ProviderInvoker: interfaceClazz is not a interface!");
    }
    this.ref = ref;
    List<Method> methods = ReflectUtils.getPublicMethod(interfaceClazz);
    for (Method method : methods) {
      String methodDesc = ReflectUtils.getMethodDesc(method);
      String methodName = method.getName();
      methodCache.putIfAbsent(methodDesc, method);
      if (methodCache.containsKey(methodName)) {
        throw new CattyException(
            "Duplicated method name: " + method.getDeclaringClass() + "#" + method
                + ". Method name excepted unique.");
      }
      methodCache.put(method.getName(), method);
      Class<?>[] exceptionTypes = method.getExceptionTypes();
      if (exceptionTypes != null && exceptionTypes.length > 0) {
        methodExceptionsCache.put(method, new HashSet<>(Arrays.asList(exceptionTypes)));
      }
    }
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    Response response = new DefaultResponse();
    String methodName = request.getMethodName();
    Method method = methodCache.get(methodName);

    if (method == null) {
      response.setStatus(ResponseStatus.OUTER_ERROR);
      response.setValue(ExceptionUtils
          .toString(new CattyException("ProviderInvoker: can't find method: " + methodName)));
      return response;
    }

    invocation.setInvokedMethod(new MethodMeta(method));
    Set<Class<?>> exceptionTypes = methodExceptionsCache.get(method);

    try {
      Object[] argsValue = request.getArgsValue();
      Object value = method.invoke(ref, argsValue);
      response.setValue(value);
      response.setStatus(ResponseStatus.OK);
    } catch (Exception e) {
      if(exceptionTypes.contains(e.getClass())) {
        response.setStatus(ResponseStatus.EXCEPTED_ERROR);
        response.setValue(ExceptionUtils.toString(e));
      } else {
        response.setStatus(ResponseStatus.INNER_ERROR);
        response.setValue(ExceptionUtils.toString(e));
      }
    } catch (Error e) {
      response.setStatus(ResponseStatus.UNKNOWN_ERROR);
      response.setValue(ExceptionUtils.toString("Unknown Error!", e));
    }
    return response;
  }

}
