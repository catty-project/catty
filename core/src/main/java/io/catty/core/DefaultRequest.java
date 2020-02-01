package io.catty.core;

import io.catty.core.service.MethodMeta;

public class DefaultRequest implements Request {

  private long requestId;
  private String interfaceName;
  private String methodName;
  private Object[] args;
  private MethodMeta methodMeta;

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

  @Override
  public MethodMeta getMethodMeta() {
    return methodMeta;
  }

  public void setMethodMeta(MethodMeta methodMeta) {
    this.methodMeta = methodMeta;
  }

  @Override
  public String toString() {
    return "DefaultRequest{" +
        "requestId=" + requestId +
        ", interfaceName='" + interfaceName + '\'' +
        ", methodName='" + methodName + '\'' +
        '}';
  }
}
