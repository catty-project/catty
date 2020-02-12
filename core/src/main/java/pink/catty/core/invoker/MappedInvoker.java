package pink.catty.core.invoker;

import java.util.Map;

public interface MappedInvoker extends Invoker, InvokerRegistry {

  void setInvokerMap(Map<String, InvokerHolder> invokerMap);

  InvokerHolder getInvoker(String invokerIdentify);

}
