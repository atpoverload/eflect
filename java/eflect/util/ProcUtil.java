package eflect.util;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/** Utility to access data from /proc/ */
// TODO(timur): should this be moved to java/eflect/data/jiffies?
public class ProcUtil {
  // /proc/pid/task
  private static final long PID = ProcessHandle.current().pid();

  /** Reads this application's thread's jiffies stat files. */
  public static ArrayList<String> readTaskStats() {
    ArrayList<String> stats = new ArrayList<String>();
    File tasks = new File(String.join(File.separator, "/proc", Long.toString(PID), "task"));
    for (File task : tasks.listFiles()) {
      try {
        stats.add(Files.readString(Path.of(task.getPath(), "stat")));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return stats;
  }

  // /proc/stat
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String SYSTEM_STAT_FILE = String.join(File.separator, "/proc", "stat");

  public static String[] readProcStat() {
    String[] stats = new String[CPU_COUNT];
    try (BufferedReader reader = Files.newBufferedReader(Path.of(SYSTEM_STAT_FILE))) {
      reader.readLine(); // first line is total summary; we need by cpu
      for (int i = 0; i < CPU_COUNT; i++) {
        stats[i] = reader.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      return stats;
    }
  }

  private ProcUtil() {}
}
