package eflect.experiments;

import eflect.CpuFreqMonitor;
import eflect.Eflect;
import eflect.util.WriterUtils;
import java.io.File;
import java.time.Duration;
import java.util.List;
import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public final class DaCapo extends Callback {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private final String outputPath;

  private Eflect eflect;
  private CpuFreqMonitor freqMonitor;
  private String benchmark;
  private int iteration = 0;

  public DaCapo(CommandLineArgs args) {
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
    eflect = new Eflect(Duration.ofMillis(41));
    freqMonitor = new CpuFreqMonitor(Duration.ofMillis(500));
    System.out.println("starting eflect");
    eflect.start();
    freqMonitor.start();
    super.start(benchmark);
  }

  @Override
  public void stop(long duration) {
    super.stop(duration);
    eflect.stop();
    freqMonitor.stop();
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

    String[] cpus = new String[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      cpus[cpu] = Integer.toString(cpu);
    }
    WriterUtils.writeCsv(
        dataDirectory.getPath(),
        "freq-" + iteration++ + ".csv",
        String.join(",", "timestamp", String.join(",", cpus)), // header
        List.of(freqMonitor.read())); // data
    freqMonitor.terminate();
  }
}
