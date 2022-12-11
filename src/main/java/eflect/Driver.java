package eflect;

import eflect.virtualization.RaplVirtualizer;
import java.util.List;

public final class Driver {
  private static int fib(int i) {
    if (i == 0 || i == 1) {
      return 1;
    } else {
      return fib(i - 1) + fib(i - 2);
    }
  }

  public static void main(String[] args) throws Exception {
    LocalEflect.getInstance().start(4);
    Thread.sleep(100);
    // fib(32);
    LocalEflect.getInstance().stop();

    List<?> data = RaplVirtualizer.virtualize(LocalEflect.getInstance().read(), i -> 0, 4);
    System.out.println(data.get(0));

    LocalEflect.getInstance().shutdown();
  }
}
