package pink.catty.core;

import pink.catty.core.meta.MetaInfo;
import pink.catty.core.service.ServiceMeta;

/**
 * This class holds meta-info for the invoker.
 */
public final class InvokerHolder {

  private ServiceMeta serviceMeta;
  private MetaInfo metaInfo;
  private Invoker invoker;

  public static InvokerHolder Of(MetaInfo metaInfo, ServiceMeta serviceMeta, Invoker invoker) {
    return new InvokerHolder(metaInfo, serviceMeta, invoker);
  }

  public InvokerHolder(MetaInfo metaInfo, ServiceMeta serviceMeta, Invoker invoker) {
    this.serviceMeta = serviceMeta;
    this.metaInfo = metaInfo;
    this.invoker = invoker;
  }

  public MetaInfo getMetaInfo() {
    return metaInfo;
  }

  public Invoker getInvoker() {
    return invoker;
  }

  public ServiceMeta getServiceMeta() {
    return serviceMeta;
  }
}
