package eflect;

import static eflect.util.WriterUtil.writeCsv;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import clerk.storage.ClassMappedListStorage;
import clerk.util.FixedPeriodClerk;
import eflect.data.Sample;
import eflect.data.jiffies.ProcDataSources;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** A clerk that collects jiffies and energy data for an intel proc system. */
public final class JiffiesCollector extends FixedPeriodClerk<Map<Class<?>, List<Sample>>> {
  private static List<Supplier<Sample>> getSources(long pid) {
    return List.of(ProcDataSources::sampleProcStat, () -> ProcDataSources.sampleTaskStats(pid));
  }

  public JiffiesCollector(long pid, ScheduledExecutorService executor, Duration period) {
    super(
        getSources(pid),
        new ClassMappedListStorage<Sample, Map<Class<?>, List<Sample>>>() {
          @Override
          public Map<Class<?>, List<Sample>> process() {
            return getData();
          }
        },
        executor,
        period);
  }

  public static void main(String[] args) throws Exception {
    int pid = Integer.parseInt(args[0]);
    String outputPath = args[1];
    Duration period = Duration.ofMillis(50);
    if (args.length > 2) {
      period = Duration.ofMillis(Integer.parseInt(args[2]));
    }

    final AtomicInteger counter = new AtomicInteger();
    ScheduledExecutorService executor =
        newScheduledThreadPool(2, r -> new Thread(r, "eflect-" + counter.getAndIncrement()));

    JiffiesCollector collector = new JiffiesCollector(pid, executor, period);
    collector.start();

    File pidFile = new File(String.join(File.separator, "/proc", Long.toString(pid)));
    while (pidFile.exists()) {
      Thread.sleep(1000);
    }

    collector.stop();

    File outputDir = new File(outputPath);
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
