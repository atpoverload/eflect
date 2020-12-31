package eflect.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/** Utility to access data from /proc */
public class ProcUtil {
  // /proc/pid/task
  private static interface libcl extends Library {
    static libcl instance = (libcl) Native.loadLibrary("c", libcl.class);

    int getpid();
  }

  private static final int pid = libcl.instance.getpid();

  public static ArrayList<String> readTaskStats() {
    ArrayList<String> stats = new ArrayList<String>();
    File tasks = new File(String.join(File.separator, "/proc", Integer.toString(pid), "task"));
    for (File task : tasks.listFiles()) {
      File stat = new File(task, "stat");
      if (!stat.canRead()) {
        continue;
      }
      try (BufferedReader reader = new BufferedReader(new FileReader(stat))) {
        stats.add(reader.readLine());
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
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(SYSTEM_STAT_FILE)))) {
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
