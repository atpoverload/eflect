package eflect.sample;

import eflect.protos.sample.CpuReading;
import eflect.protos.sample.CpuSample;
import eflect.protos.sample.ProcessSample;
import eflect.protos.sample.Sample;
import eflect.protos.sample.TaskReading;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

/** Helper for reading jiffies from /proc system. */
public final class JiffiesDataSources {
  // TODO(zain): add android friendly version that uses cat instead of trying to read through java
  /** Reads the cpu's stats and returns a {@link Sample} from it. */
  public static Sample sampleCpus() {
    String[] stats = new String[0];
    try {
      BufferedReader reader = new BufferedReader(new FileReader(CpuSource.SYSTEM_STAT_FILE));
      stats = CpuSource.readCpus(reader);
    } catch (Exception e) {
      System.out.println("unable to read " + CpuSource.SYSTEM_STAT_FILE);
    }

    return Sample.newBuilder()
        .setCpu(CpuSource.parseCpus(stats).setTimestamp(Instant.now().toEpochMilli()))
        .build();
  }

  /** Reads from a process's tasks and returns a {@link Sample} of it. */
  public static Sample sampleTasks(long pid) {
    return Sample.newBuilder()
        .setProcess(
            TasksSource.parseTasks(TasksSource.readTasks(pid))
                .setTimestamp(Instant.now().toEpochMilli()))
        .build();
  }

  private static class CpuSource {
    // system information
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final String SYSTEM_STAT_FILE = String.join(File.separator, "/proc", "stat");

    // indicies for cpu stat because there are so many
    private enum CpuIndex {
      CPU(0),
      USER(1),
      NICE(2),
      SYSTEM(3),
      IDLE(4),
      IOWAIT(5),
      IRQ(6),
      SOFTIRQ(7),
      STEAL(8),
      GUEST(9),
      GUEST_NICE(10);

      private int index;

      private CpuIndex(int index) {
        this.index = index;
      }
    }

    /** Reads the system's stat file and returns individual cpus. */
    private static String[] readCpus(BufferedReader reader) throws Exception {
      String[] stats = new String[CPU_COUNT];
      reader.readLine(); // first line is total summary; we need by cpu
      for (int i = 0; i < CPU_COUNT; i++) {
        stats[i] = reader.readLine();
      }
      return stats;
    }

    /** Turns stat strings into a {@link CpuSample}. */
    private static CpuSample.Builder parseCpus(String[] stats) {
      CpuSample.Builder sample = CpuSample.newBuilder();
      for (String statString : stats) {
        String[] stat = statString.split(" ");
        if (stat.length != 11) {
          continue;
        }
        sample.addReading(
            CpuReading.newBuilder()
                .setCpu(Integer.parseInt(stat[CpuIndex.CPU.index].substring(3)))
                .setUser(Integer.parseInt(stat[CpuIndex.USER.index]))
                .setNice(Integer.parseInt(stat[CpuIndex.NICE.index]))
                .setSystem(Integer.parseInt(stat[CpuIndex.SYSTEM.index]))
                .setIdle(Integer.parseInt(stat[CpuIndex.IDLE.index]))
                .setIowait(Integer.parseInt(stat[CpuIndex.IOWAIT.index]))
                .setIrq(Integer.parseInt(stat[CpuIndex.IRQ.index]))
                .setSoftirq(Integer.parseInt(stat[CpuIndex.SOFTIRQ.index]))
                .setSteal(Integer.parseInt(stat[CpuIndex.STEAL.index]))
                .setGuest(Integer.parseInt(stat[CpuIndex.GUEST.index]))
                .setGuestNice(Integer.parseInt(stat[CpuIndex.GUEST_NICE.index])));
      }
      return sample;
    }
  }

  private static class TasksSource {
    // task stat indicies
    private static final int STAT_LENGTH = 52;

    private enum TaskIndex {
      TID(0),
      CPU(38),
      USER(13),
      SYSTEM(14);

      private int index;

      private TaskIndex(int index) {
        this.index = index;
      }
    };

    /** Reads stat files of tasks directory of a process. */
    private static final ArrayList<String> readTasks(long pid) {
      ArrayList<String> stats = new ArrayList<String>();
      File tasks = new File(String.join(File.separator, "/proc", Long.toString(pid), "task"));
      if (!tasks.exists()) {
        return stats;
      }

      for (File task : tasks.listFiles()) {
        File statFile = new File(task, "stat");
        if (!statFile.exists()) {
          continue;
        }
        try {
          BufferedReader reader = new BufferedReader(new FileReader(statFile));
          stats.add(reader.readLine());
        } catch (Exception e) {
          System.out.println("unable to read task " + statFile + " before it terminated");
        }
      }
      return stats;
    }

    /** Turns task stat strings into a {@link ProcessSample}. */
    private static ProcessSample.Builder parseTasks(ArrayList<String> stats) {
      ProcessSample.Builder sample = ProcessSample.newBuilder();
      stats.forEach(
          statString -> {
            String[] stat = statString.split(" ");
            if (stat.length >= STAT_LENGTH) {
              // task name can be space-delimited, so there may be extra entries
              int offset = stat.length - STAT_LENGTH;
              sample.addReading(
                  TaskReading.newBuilder()
                      .setTaskId(Integer.parseInt(stat[TaskIndex.TID.index]))
                      // .setName(getName(stat, offset))
                      .setCpu(Integer.parseInt(stat[TaskIndex.CPU.index + offset]))
                      .setUser(Integer.parseInt(stat[TaskIndex.USER.index + offset]))
                      .setSystem(Integer.parseInt(stat[TaskIndex.SYSTEM.index + offset])));
            }
          });
      return sample;
    }

    /** Extracts the name from the stat string. */
    private static final String getName(String[] stat, int offset) {
      String name = String.join(" ", Arrays.copyOfRange(stat, 1, 2 + offset));
      return name.substring(1, name.length() - 1);
    }
  }

  private JiffiesDataSources() {}
}
