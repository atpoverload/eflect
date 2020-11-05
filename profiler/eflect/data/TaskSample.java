package eflect.data;

import static eflect.util.OsUtil.getProcessId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;

/** Snapshot of the application's jiffies by task. */
public class TaskSample extends Sample {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  static TaskSample readTaskStats() {
    ArrayList<String> taskJiffies = new ArrayList<String>();
    String pid = Long.toString(getProcessId());
    File tasks = new File(String.join(File.separator, "/proc", pid, "task"));
    for (File task : tasks.listFiles()) {
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
    return new TaskSample(taskJiffies.toArray(String[]::new), Instant.now());
  }

  TaskSample(String[] stats, Instant timestamp) {
    super(stats, timestamp);
  }
}
