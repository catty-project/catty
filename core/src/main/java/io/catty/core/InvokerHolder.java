package io.catty.core;

import io.catty.core.meta.MetaInfo;
import io.catty.core.service.ServiceMeta;

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
