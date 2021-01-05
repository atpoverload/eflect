package eflect.testing;

import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;

/** A profiler that provides a fake estimate of the energy consumed by the current application. */
final class DummyEflectDriver {
  public static void main(String[] args) throws Exception {
    DummyEflect eflect = new DummyEflect(Duration.ofMillis(41));
    eflect.start();
    Thread.sleep(1000);
    eflect.stop();
    WriterUtils.writeCsv(
        new File(System.getProperty("eflect.output", "."), "eflect-footprints.csv").getPath(),
        "id,name,start,end,energy,trace",
        eflect.read());
  }
}
