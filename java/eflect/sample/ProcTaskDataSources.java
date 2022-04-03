package eflect.sample;

import eflect.protos.sample.Sample;
import eflect.protos.sample.TaskStatReading;
import eflect.protos.sample.TaskStatSample;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

/** Helper for reading application jiffies from /proc/[pid]/task. */
public final class ProcTaskDataSources {
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

  /** Reads from a process's tasks and returns a {@link Sample} of it. */
  public static Sample sampleTaskStats(long pid) {
    return Sample.newBuilder()
        .setTask(readTaskStats(pid).setTimestamp(Instant.now().toEpochMilli()))
        .build();
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
        System.out.println("unable to read task " + statFile + " before it terminated");
      }
    }
    return stats;
  }

  /** Extracts the name from the stat string. */
  private static final String getName(String[] stat, int offset) {
    String name = String.join(" ", Arrays.copyOfRange(stat, 1, 2 + offset));
    return name.substring(1, name.length() - 1);
  }

  /** Turns task stat strings into a Sample. */
  private static TaskStatSample.Builder readTaskStats(long pid) {
    TaskStatSample.Builder sample = TaskStatSample.newBuilder();
    readTasks(pid)
        .forEach(
            statString -> {
              String[] stat = statString.split(" ");
              if (stat.length >= STAT_LENGTH) {
                // task name can be space-delimited, so there may be extra entries
                int offset = stat.length - STAT_LENGTH;
                sample.addReading(
                    TaskStatReading.newBuilder()
                        .setTaskId(Integer.parseInt(stat[TaskIndex.TID.index]))
                        // .setName(getName(stat, offset))
                        .setCpu(Integer.parseInt(stat[TaskIndex.CPU.index + offset]))
                        .setUser(Integer.parseInt(stat[TaskIndex.USER.index + offset]))
                        .setSystem(Integer.parseInt(stat[TaskIndex.SYSTEM.index + offset])));
              }
            });
    return sample;
  }

  private ProcTaskDataSources() {}
}
