package eflect;

import static eflect.util.WriterUtil.writeCsv;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.data.Sample;
import eflect.data.SampleCollector;
import eflect.data.jiffies.ProcDataSources;
import eflect.data.rapl.RaplDataSources;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** A monitor that watches jiffies and energy data while another process runs. */
class EflectProcessMonitor extends SampleCollector {
  private static final Duration DEFAULT_PERIOD = Duration.ofMillis(50);

  private static class EflectArgs {
    private final long pid;
    private final String outputPath;
    private final Duration period;

    private EflectArgs(long pid, String outputPath, Duration period) {
      this.pid = pid;
      this.outputPath = outputPath;
      this.period = period;
    }
  }

  private static EflectArgs parseArgs(String[] args) {
    if (args.length > 2) {
      return new EflectArgs(
          Integer.parseInt(args[0]), args[1], Duration.ofMillis(Integer.parseInt(args[2])));
    } else {
      return new EflectArgs(Integer.parseInt(args[0]), args[1], DEFAULT_PERIOD);
    }
  }

  private static List<Supplier<? extends Sample>> getSources(long pid) {
    return List.of(
        ProcDataSources::sampleProcStat,
        () -> ProcDataSources.sampleTaskStats(pid),
        RaplDataSources::sampleRapl);
  }

  private EflectProcessMonitor(long pid, ScheduledExecutorService executor, Duration period) {
    super(getSources(pid), executor, period);
  }

  public static void main(String[] args) throws Exception {
    EflectArgs eflectArgs = parseArgs(args);

    final AtomicInteger counter = new AtomicInteger();
    ScheduledExecutorService executor =
        newScheduledThreadPool(4, r -> new Thread(r, "eflect-" + counter.getAndIncrement()));

    EflectProcessMonitor collector =
        new EflectProcessMonitor(eflectArgs.pid, executor, eflectArgs.period);
    collector.start();

    File pidFile = new File(String.join(File.separator, "/proc", Long.toString(eflectArgs.pid)));
    while (pidFile.exists()) {
      Thread.sleep(1000);
    }

    collector.stop();

    File outputDir = new File(eflectArgs.outputPath);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }

    Map<Class<?>, List<Sample>> data = (Map<Class<?>, List<Sample>>) collector.read();
    for (Class<?> cls : data.keySet()) {
      String[] clsName = cls.toString().split("\\.");
      writeCsv(outputDir.getPath(), clsName[clsName.length - 1] + ".csv", data.get(cls));
    }

    executor.shutdown();
  }
}
