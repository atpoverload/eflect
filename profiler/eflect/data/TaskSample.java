package eflect.data;

import static eflect.util.OsUtil.getProcessId;

import java.time.Instant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/** Snapshot of the application's jiffies by task. */
public class TaskSample implements Sample {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private static String[] readTaskStats() {
    ArrayList<String> taskJiffies = new ArrayList<String>();
    String pid = Long.toString(getProcessId());
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
        taskJiffies.add(reader.readLine());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return taskJiffies.toArray(String[]::new);
  }

  private final String[] stats;
  private final Instant timestamp;

  public TaskSample() {
    this.stats = readTaskStats();
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
