package pink.catty.core.invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMappedInvoker implements Invoker, MappedInvoker {

  protected Map<String, InvokerHolder> invokerMap;

  public AbstractMappedInvoker() {
    this(new ConcurrentHashMap<>());
  }

  public AbstractMappedInvoker(Map<String, InvokerHolder> invokerMap) {
    this.invokerMap = invokerMap;
  }

  @Override
  public void setInvokerMap(Map<String, InvokerHolder> invokerMap) {
    this.invokerMap = invokerMap;
  }

  @Override
  public void registerInvoker(String serviceIdentify, InvokerHolder invokerHolder) {
    invokerMap.put(serviceIdentify, invokerHolder);
  }

  @Override
  public InvokerHolder unregisterInvoker(String serviceIdentify) {
    return invokerMap.remove(serviceIdentify);
  }

  @Override
  public InvokerHolder getInvoker(String invokerIdentify) {
    return invokerMap.get(invokerIdentify);
  }
}
