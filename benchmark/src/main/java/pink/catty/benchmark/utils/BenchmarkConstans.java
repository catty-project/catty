package pink.catty.benchmark.utils;

public abstract class BenchmarkConstans {

  private static final String content =
      "Rust’s central feature is ownership. Although the feature is straightforward to explain, it"
          + " has deep implications for the rest of the language.\n"
          + "\n"
          + "All programs have to manage the way they use a computer’s memory while running. Some"
          + " languages have garbage collection that constantly looks for no longer used memory as"
          + " the program runs; in other languages, the programmer must explicitly allocate and"
          + " free the memory. Rust uses a third approach: memory is managed through a system of"
          + " ownership with a set of rules that the compiler checks at compile time. None of the"
          + " ownership features slow down your program while it’s running.\n"
          + "\n"
          + "Because ownership is a new concept for many programmers, it does take some time to get"
          + " used to. The good news is that the more experienced you become with Rust and the"
          + " rules of the ownership system, the more you’ll be able to naturally develop code"
          + " that is safe and efficient. Keep at it!\n"
          + "\n"
          + "When you understand ownership, you’ll have a solid foundation for understanding the"
          + " features that make Rust unique. In this chapter, you’ll learn ownership by working"
          + " through some examples that focus on a very service data structure: strings.";

  public static final String _20_BYTES = content.substring(0, 20);
  public static final String _1000_BYTES = content.substring(0, 1000);

  public static void main(String[] args) {
    System.out.println(content.length());
    System.out.println(_20_BYTES.getBytes().length);
    System.out.println(_1000_BYTES.getBytes().length);
  }

}
