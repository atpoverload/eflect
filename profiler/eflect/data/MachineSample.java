package eflect.data;

import java.time.Instant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Snapshot of the machine's jiffies. */
public final class MachineSample implements Sample {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String STAT_FILE = String.join(File.separator, "/proc", "stat");
  private static final int[] JIFFY_INDICES = new int[] {1, 2, 3, 6, 7, 8, 9, 10};

  private static int[] readMachineJiffies() {
    int[] jiffies = new int[CPU_COUNT];
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(STAT_FILE)))) {
      reader.readLine();
      for (int i = 0; i < CPU_COUNT; i++) {
        String[] stats = reader.readLine().split(" ");
        int cpu = Integer.parseInt(stats[0].substring(3));
        for (int j: JIFFY_INDICES) {
          jiffies[cpu] += Integer.parseInt(stats[j]);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      return jiffies;
    }
  }

  private final int[] jiffies;
  private final Instant timestamp;

  public MachineSample() {
    this.jiffies = readMachineJiffies();
    this.timestamp = Instant.now();
  }

  public int[] getJiffies() {
    return jiffies;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
