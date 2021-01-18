package eflect;

import static java.util.stream.Collectors.toList;

import clerk.FixedPeriodClerk;
import clerk.Processor;
import eflect.data.Sample;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

/** A profiler that outputs timestamped cpu freqs. */
public final class CpuFreqMonitor extends FixedPeriodClerk<String> {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String FREQS_DIR_PATH =
      String.join(File.separator, "/sys", "devices", "system", "cpu");
  private static final String FREQS_DATA_PATH =
      String.join(File.separator, "cpufreq", "cpuinfo_cur_freq");

  private static String[] readCpuFreqs() {
    String[] freqs = new String[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      String freqPath =
          String.join(
              File.separator, FREQS_DIR_PATH, "cpu" + Integer.toString(cpu), FREQS_DATA_PATH);
      try (BufferedReader reader = new BufferedReader(new FileReader(freqPath))) {
        freqs[cpu] = reader.readLine();
      } catch (Exception e) {
        e.printStackTrace();
        freqs[cpu] = "-1";
      }
    }
    return freqs;
  }

  public CpuFreqMonitor(ScheduledExecutorService executor, Duration period) {
    super(
        () -> new CpuFreqSample(Instant.now(), readCpuFreqs()),
        new Processor<CpuFreqSample, String>() {
          private final ArrayList<CpuFreqSample> data = new ArrayList();

          @Override
          public void add(CpuFreqSample sample) {
            data.add(sample);
          }

          @Override
          public String process() {
            return String.join(
                System.lineSeparator(), data.stream().map(x -> x.toString()).collect(toList()));
          }
        },
        executor,
        period);
  }

  private static final class CpuFreqSample implements Sample {
    private final Instant timestamp;
    private final String[] freqs;

    private CpuFreqSample(Instant timestamp, String[] freqs) {
      this.timestamp = timestamp;
      this.freqs = freqs;
    }

    @Override
    public Instant getTimestamp() {
      return timestamp;
    }

    @Override
    public String toString() {
      String freqString = String.join(",", freqs);
      return String.join(",", timestamp.toString(), freqString);
    }
  }
}
