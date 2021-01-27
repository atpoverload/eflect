package eflect.experiments;

import static eflect.util.LoggerUtil.getLogger;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.CpuFreqMonitor;
import eflect.LinuxEflect;
import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class EflectCalmnessMonitor {
  private static final Logger logger = getLogger();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final AtomicInteger counter = new AtomicInteger();
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };

  private static EflectCalmnessMonitor instance;

  public static synchronized EflectCalmnessMonitor getInstance() {
    if (instance == null) {
      instance = new EflectCalmnessMonitor();
    }
    return instance;
  }

  private final String outputPath;

  private ScheduledExecutorService executor;
  private LinuxEflect eflect;
  private CpuFreqMonitor freqMonitor;

  private EflectCalmnessMonitor() {
    this.outputPath = System.getProperty("eflect.output", ".");
  }

  public void start(long periodMillis) {
    logger.info("starting eflect");
    if (executor == null) {
      executor = newScheduledThreadPool(5, threadFactory);
    }
    Duration period = Duration.ofMillis(periodMillis);
    if (!Duration.ZERO.equals(period)) {
      eflect = new LinuxEflect(executor, period);
    } else {
      eflect = null;
    }
    freqMonitor = new CpuFreqMonitor(executor, Duration.ofMillis(500));

    if (!Duration.ZERO.equals(period)) {
      eflect.start();
    }
    freqMonitor.start();
  }

  public void stop() {
    if (eflect != null) {
      eflect.stop();
    }
    freqMonitor.stop();
    logger.info("stopped eflect");
  }

  public void dump(String dataDirectoryName) {
    File dataDirectory = new File(outputPath, dataDirectoryName);
    if (!dataDirectory.exists()) {
      dataDirectory.mkdirs();
    }
    if (eflect != null) {
      WriterUtils.writeCsv(
          dataDirectory.getPath(),
          "footprint.csv",
          "id,name,start,end,energy,trace", // header
          eflect.read()); // data
    }

    String[] cpus = new String[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      cpus[cpu] = Integer.toString(cpu);
    }
    WriterUtils.writeCsv(
        dataDirectory.getPath(),
        "calmness.csv",
        String.join(",", "timestamp", String.join(",", cpus)), // header
        List.of(freqMonitor.read())); // data
  }

  public void dump(String dataDirectoryName, String tag) {
    File dataDirectory = new File(outputPath, dataDirectoryName);
    if (!dataDirectory.exists()) {
      dataDirectory.mkdirs();
    }
    if (eflect != null) {
      WriterUtils.writeCsv(
          dataDirectory.getPath(),
          "footprint-" + tag + ".csv",
          "id,name,start,end,energy,trace", // header
          eflect.read()); // data
    }

    String[] cpus = new String[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      cpus[cpu] = Integer.toString(cpu);
    }
    WriterUtils.writeCsv(
        dataDirectory.getPath(),
        "calmness-" + tag + ".csv",
        String.join(",", "timestamp", String.join(",", cpus)), // header
        List.of(freqMonitor.read())); // data
  }

  public void shutdown() {
    executor.shutdown();
    executor = null;
  }
}
