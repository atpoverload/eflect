package eflect.testing;

import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DummyDaCapo extends Callback {
  private DummyEflect eflect;

  public DummyDaCapo(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    eflect = new DummyEflect(Duration.ofMillis(41));
    System.out.println("starting eflect");
    eflect.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    eflect.stop();
    System.out.println("stopped eflect");
    WriterUtils.writeCsv(
        new File(System.getProperty("eflect.output", "."), "eflect-footprints.csv").getPath(),
        "id,name,start,end,energy,trace",
        eflect.read());
    eflect.terminate();
  }
}
