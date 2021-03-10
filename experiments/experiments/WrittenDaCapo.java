package eflect.experiments;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.LinuxWriter;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class WrittenDaCapo extends Callback {
  private final Duration period = Duration.ofMillis(64);
  private final ScheduledExecutorService executor = newScheduledThreadPool(2);

  private LinuxWriter monitor =
      new LinuxWriter(System.getProperty("eflect.output", "eflect-log"), executor, period);
  private int iteration = 0;

  public WrittenDaCapo(CommandLineArgs args) {
    super(args);
  }

  @Override
  public void start(String benchmark) {
    monitor.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    monitor.stop();
  }

  @Override
  public boolean runAgain() {
    boolean doRun = super.runAgain();
    if (!doRun) {
      executor.shutdown();
      File dataDirectory = new File(System.getProperty("eflect.output", "eflect-log"));
      if (!dataDirectory.exists()) {
        dataDirectory.mkdir();
      }

      if (dataDirectory.isDirectory()) {
        monitor.read();
      } else {
        System.out.println("expected a directory for " + dataDirectory + " but found a file!");
      }
    }
    return doRun;
  }
}
