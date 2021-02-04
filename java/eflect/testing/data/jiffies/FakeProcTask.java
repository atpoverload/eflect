package eflect.testing.data.jiffies;

import eflect.data.jiffies.ProcTaskSample;
import java.time.Instant;
import java.util.ArrayList;

/** A class that builds a {@link ProcTasksSample). */
public final class FakeProcTask {
  private static final String PRE_JIFFIES_BODY = FakeProcUtil.createDummyStats(10);
  private static final String PRE_CPU_BODY = FakeProcUtil.createDummyStats(24);
  private static final String END_BODY = FakeProcUtil.createDummyStats(13);

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

  private final ArrayList<FakeTaskInfo> tasks = new ArrayList<>();

  private Instant timestamp = Instant.EPOCH;

  public FakeProcTask() {}

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public void addTask(long tid, int cpu, long jiffies) {
    tasks.add(new FakeTaskInfo(tid, cpu, jiffies));
  }

  public void reset() {
    this.timestamp = Instant.EPOCH;
    tasks.clear();
  }

  public ProcTaskSample read() {
    ArrayList<String> stats = new ArrayList<>();
    for (FakeTaskInfo task : tasks) {
      stats.add(buildStat(task.tid, task.cpu, task.jiffies));
    }
    return new ProcTaskSample(timestamp, stats);
  }

  private class FakeTaskInfo {
    private final long tid;
    private final int cpu;
    private final long jiffies;

    private FakeTaskInfo(long tid, int cpu, long jiffies) {
      this.tid = tid;
      this.cpu = cpu;
      this.jiffies = jiffies;
    }
  }
}
