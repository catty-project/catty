package pink.catty.core.invoker;

public interface InvokerRegistry {

  void registerInvoker(String serviceIdentify, InvokerHolder invokerHolder);

  InvokerHolder unregisterInvoker(String serviceIdentify);

}
