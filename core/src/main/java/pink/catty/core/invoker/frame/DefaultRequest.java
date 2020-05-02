/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.core.invoker.frame;

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

  @Override
  public String toString() {
    return "DefaultRequest{" +
        "requestId=" + requestId +
        ", interfaceName='" + interfaceName + '\'' +
        ", methodName='" + methodName + '\'' +
        '}';
  }
}
