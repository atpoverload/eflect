package eflect.utils;

import static jrapl.Rapl.getEnergyStats;
import static jrapl.Rapl.SOCKET_COUNT;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/** Utilities to access the operating system. */
// TODO(timur): there's only one method here...
public final class OsUtils {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String STAT_FILE = String.join(File.separator, "/proc", "stat");

  /** Wrapper for libc methods. This is locked so that users stick to the public API. */
  /** Dummy wrapper for the libc library instance. */
  private static interface libcl extends Library {
    static libcl instance = (libcl) Native.loadLibrary("c", libcl.class);
    int getpid();
  }

  private static AtomicInteger pid = new AtomicInteger(0);

  public static int getProcessId() {
    if (pid.get() != -1) {
      try {
          pid.set(libcl.instance.getpid());
      } catch (UnsatisfiedLinkError e) {
        pid.set(-1);
      }
    }
    return pid.get();
  }

  public static void setProcessId(int process) {
    pid.set(process);
  }

  // these only works for specific architectures
  public static int cpuToSocket(int cpu) {
    return cpu * SOCKET_COUNT / CPU_COUNT;
  }

  public static int[] readMachineJiffies() {
    int[] jiffies = new int[SOCKET_COUNT];
    File cpus = new File(STAT_FILE);

    try (BufferedReader reader = new BufferedReader(new FileReader(cpus))) {
      reader.readLine();
      for (int i = 0; i < CPU_COUNT; i++) {
        String[] stats = reader.readLine().split(" ");
        int socket = cpuToSocket(i);
        int jiffy = Integer.parseInt(stats[1])
          + Integer.parseInt(stats[2])
          + Integer.parseInt(stats[3])
          + Integer.parseInt(stats[6])
          + Integer.parseInt(stats[7])
          + Integer.parseInt(stats[8])
          + Integer.parseInt(stats[9])
          + Integer.parseInt(stats[10]);

        jiffies[socket] += jiffy;
      }
    } catch (Exception e) {
      /* unsafe */
    } finally {
      return jiffies;
    }
  }

  public static int[] readApplicationJiffies() {
    int[] jiffies = new int[SOCKET_COUNT];
    File[] tasks = new File(String.join(
      File.separator,
      "/proc",
      Integer.toString(getProcessId()),
      "task"))
    .listFiles();

    for (int i = 0; i < tasks.length; i++) {
      int tid = Integer.parseInt(tasks[i].getName());
      String task = String.join(
        File.separator,
        "/proc",
        Integer.toString(getProcessId()),
        "task",
        Integer.toString(tid),
        "stat");

      if (Files.notExists(Paths.get(task)))
        continue;

      try {
        String[] stat = Files.readString(Paths.get(task)).split(" ");

        int offset = stat.length - 52;
        int socket = cpuToSocket(Integer.parseInt(stat[38 + offset]));
        int jiffy = Integer.parseInt(stat[13 + offset]) + Integer.parseInt(stat[14 + offset]);

        jiffies[socket] += jiffy;
      } catch (Exception e) {
        // unsafe
      }
    }
    return jiffies;
  }

  // rearrangement of jrapl's data
  public static double[] readEnergy() {
    double[] energy = new double[SOCKET_COUNT];
    double[][] stats = getEnergyStats();
    for (int i = 0; i < SOCKET_COUNT; i++) {
      double stat = 0;
      for (double e: stats[i]) {
        stat += e;
      }
      energy[i] = stat;
    }
    return energy;
  }
}
