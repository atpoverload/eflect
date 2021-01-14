package eflect.experiments;

import static clerk.util.ClerkUtil.getLogger;

import eflect.CpuFreqMonitor;
import eflect.LinuxEflect;
import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

public final class EflectProfiler {
  private static final Logger logger = getLogger();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private static EflectProfiler instance;

  public static synchronized EflectProfiler getInstance() {
    if (instance == null) {
      instance = new EflectProfiler();
    }
    return instance;
  }

  private final String outputPath;

  private LinuxEflect eflect;
  private CpuFreqMonitor freqMonitor;

  private EflectProfiler() {
    this.outputPath = System.getProperty("eflect.output", ".");
  }

  public void start(Duration period) {
    logger.info("starting eflect");
    if (!Duration.ZERO.equals(period)) {
      eflect = new LinuxEflect(period);
    } else {
      eflect = null;
    }
    freqMonitor = new CpuFreqMonitor(Duration.ofMillis(500));

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

  public void dump(String dataDirectoryName, int iteration) {
    File dataDirectory = new File(outputPath, dataDirectoryName);
    if (!dataDirectory.exists()) {
      dataDirectory.mkdirs();
    }
    if (eflect != null) {
      WriterUtils.writeCsv(
          dataDirectory.getPath(),
          "footprint-" + iteration + ".csv",
          "id,name,start,end,energy,trace", // header
          eflect.read()); // data
      eflect.terminate();
    }

    String[] cpus = new String[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      cpus[cpu] = Integer.toString(cpu);
    }
    WriterUtils.writeCsv(
        dataDirectory.getPath(),
        "calmness-" + iteration + ".csv",
        String.join(",", "timestamp", String.join(",", cpus)), // header
        List.of(freqMonitor.read())); // data
    freqMonitor.terminate();
  }
}
