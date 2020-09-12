package eflect;

import eflect.EflectProfiler;

public class Driver {
  public static void main(String[] args) throws Exception {
    EflectProfiler.start();
    for (int i = 0; i < 10; i++) {
      Thread.sleep(1000);
      System.out.println(EflectProfiler.dump());
    }
    EflectProfiler.stop();

    System.out.println(EflectProfiler.dump());

    System.out.println(EflectProfiler.dump());
  }
}
