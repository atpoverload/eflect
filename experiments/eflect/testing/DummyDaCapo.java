package eflect.testing;

import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DummyDaCapo extends Callback {
  private final String outputPath;

  private DummyEflect eflect;
  private String benchmark;
  private int iteration = 0;

  public DummyDaCapo(CommandLineArgs args) {
    super(args);
    outputPath = System.getProperty("eflect.output", ".");
    File output = new File(outputPath);
    if (!output.exists()) {
      output.mkdir();
    }
  }

  @Override
  public void start(String benchmark) {
    this.benchmark = benchmark;
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

    File dataDirectory = new File(outputPath, benchmark);
    if (!dataDirectory.exists()) {
      dataDirectory.mkdir();
    }
    WriterUtils.writeCsv(
        dataDirectory.getPath(),
        "eflect-footprint-" + iteration++ + ".csv",
        "id,name,start,end,energy,trace", // header
        eflect.read()); // data
    eflect.terminate();
  }
}
