package pink.catty.core.invoker;

public interface Request {

  long getRequestId();

  void setRequestId(long requestId);

  String getInterfaceName();

  void setInterfaceName(String interfaceName);

  String getMethodName();

  void setMethodName(String methodName);

  Object[] getArgsValue();

  void setArgsValue(Object[] argsValue);

}
