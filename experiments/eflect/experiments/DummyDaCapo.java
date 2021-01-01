package eflect.experiments;

import clerk.Clerk;
import eflect.DummyEflect;
import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DummyDaCapo extends Callback {
  private Clerk<?> clerk;

  public DummyDaCapo(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    clerk = DummyEflect.newEflectClerk(Duration.ofMillis(41));
    System.out.println("starting eflect");
    clerk.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    clerk.stop();
    System.out.println("stopped eflect");
    WriterUtils.writeCsv(
        new File(System.getProperty("eflect.output", "."), "eflect-footprints.csv").getPath(),
        "id,name,start,end,energy,trace",
        (Iterable<?>) clerk.read());
  }
}
