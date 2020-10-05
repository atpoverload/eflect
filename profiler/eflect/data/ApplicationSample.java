package eflect.data;

import static eflect.utils.OsUtils.getProcessId;

import java.time.Instant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Snapshot of the application's jiffies. */
public class ApplicationSample implements Sample {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int STAT_LENGTH = 52;
  private static final int CPU_INDEX = 38;
  private static final int[] JIFFY_INDICES = new int[] {13, 14};

  private static long[] readApplicationJiffies() {
    long[] jiffies = new long[CPU_COUNT];
    String pid = Integer.toString(getProcessId());
    File tasks = new File(String.join(
      File.separator,
      "/proc",
      pid,
      "task"));
    for (File task: tasks.listFiles()) {
      File statFile = new File(task, "stat");
      if (!statFile.canRead()) {
        continue;
      }
      try (BufferedReader reader = new BufferedReader(new FileReader(statFile))) {
        String[] stats = reader.readLine().split(" ");

        int offset = stats.length - STAT_LENGTH;
        int cpu = Integer.parseInt(stats[CPU_INDEX + offset]);
        for (int i: JIFFY_INDICES) {
          jiffies[cpu] += Long.parseLong(stats[i + offset]);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return jiffies;
  }

  private final long[] jiffies;
  private final Instant timestamp;

  public ApplicationSample() {
    this.jiffies = readApplicationJiffies();
    this.timestamp = Instant.now();
  }

  public long[] getJiffies() {
    return jiffies;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
