package eflect.testing.data.jiffies;

import eflect.data.jiffies.ProcTaskSample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.Supplier;

/** A class that builds a {@link ProcTasksSample). */
// TODO(timurbey): this is a strange dependency chain
public final class DummyProcTask implements Supplier<ProcTaskSample> {
  private static final String PRE_JIFFIES_BODY = ProcUtil.createDummyStats(10);
  private static final String PRE_CPU_BODY = ProcUtil.createDummyStats(24);
  private static final String END_BODY = ProcUtil.createDummyStats(13);

  private static final String buildStat(long tid, int cpu, long jiffies) {
    return String.join(
        " ",
        Long.toString(tid),
        "(fake-name)",
        "R",
        PRE_JIFFIES_BODY,
        Long.toString(jiffies),
        PRE_CPU_BODY,
        Integer.toString(cpu),
        END_BODY);
  }

  private ArrayList<TaskInfo> tasks = new ArrayList<>();

  private int cpu = 0;
  private int jiffies = 0;
  private Instant timestamp = Instant.EPOCH;

  public DummyProcTask() {}

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public void setCpu(int cpu) {
    this.cpu = cpu;
  }

  public void addTask(long tid, int cpu, long jiffies) {
    tasks.add(new TaskInfo(tid, cpu, jiffies));
  }

  public void clear() {
    tasks.clear();
  }

  @Override
  public ProcTaskSample get() {
    ArrayList<String> stats = new ArrayList<>();
    for (TaskInfo task : tasks) {
      stats.add(buildStat(task.tid, task.cpu, task.jiffies));
    }
    return new ProcTaskSample(timestamp, stats);
  }

  private class TaskInfo {
    private final long tid;
    private final int cpu;
    private final long jiffies;

    private TaskInfo(long tid, int cpu, long jiffies) {
      this.tid = tid;
      this.cpu = cpu;
      this.jiffies = jiffies;
    }
  }
}
