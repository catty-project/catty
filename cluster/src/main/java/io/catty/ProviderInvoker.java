package io.catty;

import io.catty.Response.ResponseStatus;
import io.catty.api.DefaultResponse;
import io.catty.codec.Serialization;
import io.catty.exception.CattyException;
import io.catty.utils.ExceptionUtils;
import io.catty.utils.ReflectUtils;
import java.io.IOException;
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
  private Serialization serialization;

  /**
   * cache all interface's methods
   */
  public ProviderInvoker(T ref, Class<T> interfaceClazz, Serialization serialization) {
    if (!interfaceClazz.isInterface()) {
      throw new CattyException("ProviderInvoker: interfaceClazz is not a interface!");
    }
    this.ref = ref;
    List<Method> methods = ReflectUtils.parseMethod(interfaceClazz);
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
    this.serialization = serialization;
  }

  @Override
  public Response invoke(Request request, Runtime runtime) {
    Response response = new DefaultResponse();
    String methodName = request.getMethodName();
    Method method = methodCache.get(methodName);

    if (method == null) {
      response.setStatus(ResponseStatus.OUTER_ERROR);
      response.setValue(ExceptionUtils
          .toString(new CattyException("ProviderInvoker: can't find method: " + methodName)));
      return response;
    }

    Set<Class<?>> exceptionTypes = methodExceptionsCache.get(method);

    try {
      Object[] argsValue = resolveArgsValue(request.getArgsValue(), method);
      Object value = method.invoke(ref, argsValue);
      if (value != null) {
        response.setValue(serialization.serialize(value));
      }
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

  @SuppressWarnings("unchecked")
  private Object[] resolveArgsValue(Object[] args, Method method) throws IOException {
    if (args == null) {
      return null;
    }
    Object[] resolvedArgs = new Object[args.length];
    Class<?>[] parameterTypes = method.getParameterTypes();
    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof byte[]) {
        resolvedArgs[i] = serialization.deserialize((byte[]) args[i], parameterTypes[i]);
      }
    }
    return resolvedArgs;
  }

}
