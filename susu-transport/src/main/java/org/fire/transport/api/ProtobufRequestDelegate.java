package org.fire.transport.api;

import com.google.protobuf.Any;
import com.google.protobuf.Any.Builder;
import com.google.protobuf.Message;
import org.fire.core.Request;
import org.fire.core.codec.generated.SusuProtocol;

public class ProtobufRequestDelegate implements Request {

  private SusuProtocol.Request.Builder builder;
  private SusuProtocol.Request delegatedRequest;

  public ProtobufRequestDelegate() {
    builder = SusuProtocol.Request.newBuilder();
  }

  public ProtobufRequestDelegate(SusuProtocol.Request delegatedRequest) {
    this.delegatedRequest = delegatedRequest;
  }

  @Override
  public long getRequestId() {
    if (delegatedRequest == null) {
      return 0;
    }
    return delegatedRequest.getRequestId();
  }

  @Override
  public String getInterfaceName() {
    if (delegatedRequest == null) {
      return null;
    }
    return delegatedRequest.getInterfaceName();
  }

  @Override
  public String getMethodName() {
    if (delegatedRequest == null) {
      return null;
    }
    return delegatedRequest.getMethodName();
  }

  @Override
  public Object[] getArgsValue() {
    if (delegatedRequest == null) {
      return null;
    }
    return delegatedRequest.getArgsList().toArray();
  }

  @Override
  public void setRequestId(long requestId) {
    builder.setRequestId(requestId);
  }

  @Override
  public void setInterfaceName(String interfaceName) {
    builder.setInterfaceName(interfaceName);
  }

  @Override
  public void setMethodName(String methodName) {
    builder.setMethodName(methodName);
  }

  @Override
  public void setArgsValue(Object[] argsValue) {
    if (argsValue == null) {
      return;
    }
    for (Object arg : argsValue) {
      if (arg instanceof Any) {
        builder.addArgs((Any) arg);
      } else if (arg instanceof Builder) {
        builder.addArgs((Builder) arg);
      } else if (arg instanceof Message) {
        builder.addArgs(Any.pack((Message) arg));
      } else {
        throw new IllegalArgumentException();
      }
    }
  }

  @Override
  public void build() {
    if (delegatedRequest == null) {
      delegatedRequest = builder.build();
    }
  }

  public SusuProtocol.Request getDelegatedRequest() {
    return delegatedRequest;
  }
}
