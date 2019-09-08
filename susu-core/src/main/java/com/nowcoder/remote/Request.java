package com.nowcoder.remote;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class Request {

  private long requestId;

  private String interfaceName;

  private String methodName;

  private String argsType;

  private Object[] argsValue;

  public long getRequestId() {
    return requestId;
  }

  public void setRequestId(long requestId) {
    this.requestId = requestId;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getArgsType() {
    return argsType;
  }

  public void setArgsType(String argsType) {
    this.argsType = argsType;
  }

  public Object[] getArgsValue() {
    return argsValue;
  }

  public void setArgsValue(Object[] argsValue) {
    this.argsValue = argsValue;
  }
}
