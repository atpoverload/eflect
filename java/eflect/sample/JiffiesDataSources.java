package eflect.sample;

import eflect.protos.sample.CpuSample;
import eflect.protos.sample.CpuStat;
import eflect.protos.sample.Sample;
import eflect.protos.sample.TaskSample;
import eflect.protos.sample.TaskStat;
import eflect.util.LoggerUtil;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/** A clerk that collects jiffies and energy data for an intel linux system. */
public final class JiffiesDataSources {
  private static final Logger logger = LoggerUtil.getLogger();
  private static final long PID = ProcessHandle.current().pid();

  public static Sample sampleCpus() {
    return Sample.newBuilder()
        .setCpu(CpuSource.readCpuStats().setTimestamp(Instant.now().toEpochMilli()))
        .build();
  }

  /** Reads from a process's task directory and returns a sample from it. */
  public static Sample sampleTasks(long pid) {
    return Sample.newBuilder()
        .setTask(TaskSource.readTaskStats(pid).setTimestamp(Instant.now().toEpochMilli()))
        .build();
  }

  /** Reads this application's thread's stat files and returns a timestamped sample. */
  public static Sample sampleTasks() {
    return sampleTasks(PID);
  }

  /** Wrapper around /proc/stat. */
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
    public static String[] readCpus() {
      String[] stats = new String[CPU_COUNT];
      try (BufferedReader reader = Files.newBufferedReader(Path.of(SYSTEM_STAT_FILE))) {
        reader.readLine(); // first line is total summary; we need by cpu
        for (int i = 0; i < CPU_COUNT; i++) {
          stats[i] = reader.readLine();
        }
      } catch (Exception e) {
        logger.info("unable to read " + SYSTEM_STAT_FILE);
      } finally {
        return stats;
      }
    }

    /** Turns task stat strings into a Sample. */
    private static CpuSample.Builder readCpuStats() {
      CpuSample.Builder sample = CpuSample.newBuilder();
      for (String statString : readCpus()) {
        String[] stat = statString.split(" ");
        if (stat.length != 11) {
          continue;
        }
        sample.addStat(
            CpuStat.newBuilder()
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

  /** Wrapper around /proc/pid/task/tid/stat. */
  private static class TaskSource {
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
          stats.add(Files.readString(Path.of(statFile.getPath())));
        } catch (Exception e) {
          logger.info("unable to read task " + statFile + " before it terminated");
        }
      }
      return stats;
    }

    private static final String getName(String[] stat, int offset) {
      String name = String.join(" ", Arrays.copyOfRange(stat, 1, 2 + offset));
      return name.substring(1, name.length() - 1);
    }

    /** Turns task stat strings into a Sample. */
    private static TaskSample.Builder readTaskStats(long pid) {
      TaskSample.Builder sample = TaskSample.newBuilder();
      readTasks(pid)
          .forEach(
              statString -> {
                String[] stat = statString.split(" ");
                if (stat.length >= STAT_LENGTH) {
                  int offset = stat.length - STAT_LENGTH;
                  sample.addStat(
                      TaskStat.newBuilder()
                          .setTaskId(Integer.parseInt(stat[TaskIndex.TID.index]))
                          // .setName(getName(stat, offset))
                          .setCpu(Integer.parseInt(stat[TaskIndex.CPU.index + offset]))
                          .setUser(Integer.parseInt(stat[TaskIndex.USER.index + offset]))
                          .setSystem(Integer.parseInt(stat[TaskIndex.SYSTEM.index + offset])));
                }
              });
      return sample;
    }
  }

  private JiffiesDataSources() {}
}
