package eflect.experiments;

import eflect.Eflect;
import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapo extends Callback {
  private Eflect eflect;

  public DaCapo(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    eflect = new Eflect(Duration.ofMillis(41));
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
