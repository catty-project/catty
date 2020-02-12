package pink.catty.core.invoker;

public interface LinkedInvoker extends Invoker {

  void setNext(Invoker next);

  Invoker getNext();

}
