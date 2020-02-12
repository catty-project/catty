package pink.catty.core.invoker;

public abstract class AbstractLinkedInvoker implements Invoker, LinkedInvoker {

  protected Invoker next;

  public AbstractLinkedInvoker() {
  }

  public AbstractLinkedInvoker(Invoker next) {
    this.next = next;
  }

  @Override
  public void setNext(Invoker next) {
    this.next = next;
  }

  @Override
  public Invoker getNext() {
    return next;
  }
}
