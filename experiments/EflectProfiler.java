package eflect.experiments;

import eflect.CpuFreqMonitor;
import eflect.Eflect;
import eflect.util.WriterUtils;
import java.time.Duration;

public class EflectProfiler {
  private static EflectProfiler instance;

  private final String outputPath;

  private Eflect eflect;
  private CpuFreqMonitor freqMonitor;

  private EflectProfiler() {
    this.outputPath = System.getProperty("eflect.output", ".");
  }

  public static EflectProfiler getInstance() {
    if (instance == null) {
      instance = new EflectProfiler();
    }
    return getInstance(null);
  }

  public void start(Duration period) {
    eflect = new Eflect(period);
    freqMonitor = new CpuFreqMonitor(Duration.ofMillis(500));

    eflect.start();
    freqMonitor.start();
  }

  public void stop() {
    eflect.stop();
    freqMonitor.stop();
  }

  public void dump(String dataDirectoryName, int iteration) {
    File dataDirectory = new File(outputPath, dataDirectoryName);
    if (!dataDirectory.exists()) {
      dataDirectory.mkdir();
    }
    WriterUtils.writeCsv(
        dataDirectory.getPath(),
        "footprint-" + iteration + ".csv",
        "id,name,start,end,energy,trace", // header
        eflect.read()); // data
    eflect.terminate();

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
