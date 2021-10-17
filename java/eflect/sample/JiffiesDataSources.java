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
import java.util.logging.Logger;

/** A clerk that collects jiffies and energy data for an intel linux system. */
public final class JiffiesDataSources {
  private static final Logger logger = LoggerUtil.getLogger();
  // system information
  private static final long PID = ProcessHandle.current().pid();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String SYSTEM_STAT_FILE = String.join(File.separator, "/proc", "stat");
  // task stat indicies
  private static final int STAT_LENGTH = 52;
  private static final int TID_INDEX = 0;
  private static final int CPU_INDEX = 38;
  private static final int USER_JIFFIES_INDEX = 13;
  private static final int KERNEL_JIFFIES_INDEX = 14;

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
  private static CpuSample parseCpuStats(Instant timestamp, String[] cpuStats) {
    CpuSample.Builder sample = CpuSample.newBuilder().setTimestamp(timestamp.toEpochMilli());
    for (String statString : cpuStats) {
      String[] stat = statString.split(" ");
      if (stat.length != 11) {
        continue;
      }
      int offset = stat.length - STAT_LENGTH;

      // TODO(timur): turn names back on if we need them
      // String name = String.join(" ", Arrays.copyOfRange(stat, 1, 2 + offset));
      // name = name.substring(1, name.length() - 1);

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
    return sample.build();
  }

  public static Sample sampleCpus() {
    return Sample.newBuilder().setCpu(parseCpuStats(Instant.now(), readCpus())).build();
  }

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

  /** Turns task stat strings into a Sample. */
  private static TaskSample parseTaskStats(Instant timestamp, ArrayList<String> taskStats) {
    TaskSample.Builder sample = TaskSample.newBuilder().setTimestamp(timestamp.toEpochMilli());
    taskStats.forEach(
        statString -> {
          String[] stat = statString.split(" ");
          if (stat.length >= STAT_LENGTH) {
            int offset = stat.length - STAT_LENGTH;

            // TODO(timur): turn names back on if we need them
            // String name = String.join(" ", Arrays.copyOfRange(stat, 1, 2 + offset));
            // name = name.substring(1, name.length() - 1);

            sample.addStat(
                TaskStat.newBuilder()
                    .setTaskId(Integer.parseInt(stat[TID_INDEX]))
                    // .setName(name)
                    .setCpu(Integer.parseInt(stat[CPU_INDEX + offset]))
                    .setUser(Integer.parseInt(stat[USER_JIFFIES_INDEX + offset]))
                    .setSystem(Integer.parseInt(stat[KERNEL_JIFFIES_INDEX + offset])));
          }
        });
    return sample.build();
  }

  /** Reads from a process's task directory and returns a sample from it. */
  public static Sample sampleTasks(long pid) {
    return Sample.newBuilder().setTask(parseTaskStats(Instant.now(), readTasks(pid))).build();
  }

  /** Reads this application's thread's stat files and returns a timestamped sample. */
  public static Sample sampleTasks() {
    return sampleTasks(PID);
  }

  private JiffiesDataSources() {}
}
