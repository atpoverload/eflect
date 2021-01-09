package eflect;

import static java.util.stream.Collectors.toList;

import clerk.FixedPeriodClerk;
import clerk.Processor;
import eflect.data.Sample;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/** A profiler that estimates the energy consumed by the current application. */
public final class CpuFreqMonitor extends FixedPeriodClerk<String> {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String FREQS_DIR_PATH =
      String.join(System.lineSeparator(), "sys", "devices", "system", "cpu", "cpu");
  private static final String FREQS_DATA_PATH =
      String.join(System.lineSeparator(), "cpufreq", "cpuinfo_cur_freq");

  private static String[] readCpuFreqs() {
    String[] freqs = new String[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      String freqPath =
          String.join(
              System.lineSeparator(), FREQS_DIR_PATH, Integer.toString(cpu), FREQS_DATA_PATH);
      try (BufferedReader reader = new BufferedReader(new FileReader(freqPath))) {
        freqs[cpu] = reader.readLine();
      } catch (Exception e) {
        freqs[cpu] = "-1";
      }
    }
    return freqs;
  }

  public CpuFreqMonitor(Duration period) {
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

  public static void main(String[] args) throws Exception {
    CpuFreqMonitor clerk = new CpuFreqMonitor(Duration.ofMillis(512));
    clerk.start();
    Thread.sleep(10000);
    clerk.stop();
    System.out.println(clerk.read());
  }
}
