package eflect;

import static eflect.util.WriterUtil.writeCsv;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import clerk.storage.ClassMappedListStorage;
import clerk.util.FixedPeriodClerk;
import eflect.data.EnergySample;
import eflect.data.EnergySample.Component;
import eflect.data.Sample;
import eflect.data.async.AsyncProfilerDataSources;
import eflect.data.jiffies.ProcDataSources;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import jrapl.Rapl;

/** A clerk that collects jiffies and energy data for an intel proc system. */
public final class EflectSampleCollector extends FixedPeriodClerk<Map<Class<?>, List<Sample>>> {
  private static Sample sampleRapl() {
    double[][] sample = Rapl.getInstance().getEnergyStats();
    double[][] energy = new double[sample.length][4];
    for (int socket = 0; socket < sample.length; socket++) {
      energy[socket][Component.CPU] = sample[socket][Component.CPU];
      energy[socket][Component.DRAM] = sample[socket][Component.DRAM];
      energy[socket][Component.PACKAGE] = sample[socket][Component.PACKAGE];
      energy[socket][Component.GPU] = -1;
    }
    return new EnergySample(Instant.now(), energy);
  }

  private static List<Supplier<Sample>> getSources() {
    return List.of(
        ProcDataSources::sampleProcStat,
        ProcDataSources::sampleTaskStats,
        EflectSampleCollector::sampleRapl,
        AsyncProfilerDataSources::sampleAsyncProfiler);
  }

  private static List<Supplier<Sample>> getSources(long pid) {
    return List.of(
        ProcDataSources::sampleProcStat,
        () -> ProcDataSources.sampleTaskStats(pid),
        EflectSampleCollector::sampleRapl);
  }

  public EflectSampleCollector(ScheduledExecutorService executor, Duration period) {
    super(
        getSources(),
        new ClassMappedListStorage<Sample, Map<Class<?>, List<Sample>>>() {
          @Override
          public Map<Class<?>, List<Sample>> process() {
            return getData();
          }
        },
        executor,
        period);
  }

  private EflectSampleCollector(long pid, ScheduledExecutorService executor, Duration period) {
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
        newScheduledThreadPool(4, r -> new Thread(r, "eflect-" + counter.getAndIncrement()));

    EflectSampleCollector collector = new EflectSampleCollector(pid, executor, period);
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
