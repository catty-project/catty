package pink.catty.core;

import pink.catty.core.meta.MetaInfo;
import pink.catty.core.service.MethodMeta;
import pink.catty.core.service.ServiceMeta;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Invocation {

  public enum InvokerLinkTypeEnum {
    CONSUMER,
    PROVIDER,
    ;
  }

  private Map<String, Object> attribute = new ConcurrentHashMap<>();

  private MetaInfo metaInfo;

  private ServiceMeta serviceMeta;

  private MethodMeta invokedMethod;

  private InvokerLinkTypeEnum linkTypeEnum;

  private Object target;

  public Object getTarget() {
    return target;
  }

  public void setTarget(Object target) {
    this.target = target;
  }

  public MethodMeta getInvokedMethod() {
    return invokedMethod;
  }

  public Invocation(InvokerLinkTypeEnum linkTypeEnum) {
    this.linkTypeEnum = linkTypeEnum;
  }

  public void setInvokedMethod(MethodMeta invokedMethod) {
    this.invokedMethod = invokedMethod;
  }

  public InvokerLinkTypeEnum getLinkTypeEnum() {
    return linkTypeEnum;
  }

  public MetaInfo getMetaInfo() {
    return metaInfo;
  }

  public ServiceMeta getServiceMeta() {
    return serviceMeta;
  }

  public void setMetaInfo(MetaInfo metaInfo) {
    this.metaInfo = metaInfo;
  }

  public void setServiceMeta(ServiceMeta serviceMeta) {
    this.serviceMeta = serviceMeta;
  }

  @Override
  public String toString() {
    return invokedMethod.getMethod().toString();
  }
}
