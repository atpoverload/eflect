package eflect.data;

import java.time.Instant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Snapshot of the machine's jiffies as described by /proc/stat. */
public final class MachineSample implements Sample {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String STAT_FILE = String.join(File.separator, "/proc", "stat");

  private static String[] readMachineStats() {
    String[] stats = new String[CPU_COUNT];
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(STAT_FILE)))) {
      reader.readLine(); // first line is total summary; we'll need by socket
      for (int i = 0; i < CPU_COUNT; i++) {
        stats[i] = reader.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      return stats;
    }
  }

  private final String[] stats;
  private final Instant timestamp;

  public MachineSample() {
    this.stats = readMachineStats();
    this.timestamp = Instant.now();
  }

  @Override
  public String[] getStats() {
    return stats;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
