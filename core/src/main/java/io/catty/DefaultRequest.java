package io.catty;

import io.catty.core.Request;

public class DefaultRequest implements Request {

  private long requestId;

  private String interfaceName;

  private String methodName;

  private Object[] args;

  public DefaultRequest() {
  }

  public DefaultRequest(long requestId, String interfaceName, String methodName,
      Object[] args) {
    this.requestId = requestId;
    this.interfaceName = interfaceName;
    this.methodName = methodName;
    this.args = args;
  }

  @Override
  public long getRequestId() {
    return requestId;
  }

  @Override
  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

  @Override
  public String getInterfaceName() {
    return interfaceName;
  }

  @Override
  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  @Override
  public String getMethodName() {
    return methodName;
  }

  @Override
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  @Override
  public Object[] getArgsValue() {
    return args;
  }

  @Override
  public void setArgsValue(Object[] argsValue) {
    this.args = argsValue;
  }

}
