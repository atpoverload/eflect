package eflect.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.util.Timestamps;
import java.time.Instant;
import org.junit.Test;

public class JiffiesTest {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final long PID = ProcessHandle.current().pid();

  @Test
  public void procStat_smokeTest() {
    Instant start = Instant.now();
    CpuSample sample = Jiffies.sampleCpus();
    Instant end = Instant.now();

    assertTimestamp(Instant.ofEpochSecond(
      sample.getTimestamp().getSeconds(), sample.getTimestamp().getNanos()), start, end);
    int cpuCount = sample.getReadingCount();
    assertEquals(
        String.format("expected %d cpus but got %d", CPU_COUNT, cpuCount), cpuCount, CPU_COUNT);
  }

  @Test
  public void procTaskStat_smokeTest() {
    // TODO(timur): can we get the threads beyond java?
    int expectedThreadCount = Thread.activeCount();
    Instant start = Instant.now();
    TaskSample sample = Jiffies.sampleTasks(PID);
    Instant end = Instant.now();

    assertTimestamp(Instant.ofEpochSecond(
      sample.getTimestamp().getSeconds(), sample.getTimestamp().getNanos()), start, end);
    int taskCount = sample.getReadingCount();
    assertTrue(
        String.format("expected at least %d threads but got %d", expectedThreadCount, taskCount),
        expectedThreadCount <= taskCount);
    long uniqueTaskCount =
        sample.getReadingList().stream().map(stat -> stat.getTaskId()).distinct().count();
    assertEquals(
        String.format("expected %d unique tasks but got %d", uniqueTaskCount, taskCount),
        uniqueTaskCount,
        taskCount);
    assertTrue(
        String.format("no task with pid %d", PID),
        sample.getReadingList().stream().anyMatch(stat -> stat.getTaskId() == PID));
  }

  // TODO(timur): this is too soft of an assertion, we'd like to provide a controlled time source
  private static void assertTimestamp(Instant timestamp, Instant start, Instant end) {
    String error = String.format("timestamp %s outside of range (%s -> %s)", timestamp, start, end);
    assertTrue(error, start.toEpochMilli() <= timestamp.toEpochMilli());
    assertTrue(error, timestamp.toEpochMilli() <= end.toEpochMilli());
  }
}
