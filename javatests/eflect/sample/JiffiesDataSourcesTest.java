package eflect.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eflect.protos.sample.Sample;
import java.time.Instant;
import org.junit.Test;

public class JiffiesDataSourcesTest {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final long PID = ProcessHandle.current().pid();

  @Test
  public void procStat_smokeTest() {
    Instant start = Instant.now();
    Sample sample = JiffiesDataSources.sampleCpus();
    Instant end = Instant.now();

    assertTrue(String.format("expected CPU but got %s", sample.getDataCase()), sample.hasCpu());
    assertTimestamp(Instant.ofEpochMilli(sample.getCpu().getTimestamp()), start, end);
    int cpuCount = sample.getCpu().getReadingCount();
    assertEquals(
        String.format("expected %d cpus but got %d", CPU_COUNT, cpuCount), cpuCount, CPU_COUNT);
  }

  @Test
  public void procTaskStat_smokeTest() {
    // TODO(timur): can we get the threads beyond java?
    int expectedThreadCount = Thread.activeCount();
    Instant start = Instant.now();
    Sample sample = JiffiesDataSources.sampleTasks(PID);
    Instant end = Instant.now();

    assertTrue(
        String.format("expected PROCESS but got %s", sample.getDataCase()), sample.hasProcess());
    assertTimestamp(Instant.ofEpochMilli(sample.getProcess().getTimestamp()), start, end);
    int taskCount = sample.getProcess().getReadingCount();
    assertTrue(
        String.format("expected at least %d threads but got %d", expectedThreadCount, taskCount),
        expectedThreadCount <= taskCount);
    long uniqueTaskCount =
        sample
            .getProcess()
            .getReadingList()
            .stream()
            .map(stat -> stat.getTaskId())
            .distinct()
            .count();
    assertEquals(
        String.format("expected %d unique tasks but got %d", uniqueTaskCount, taskCount),
        uniqueTaskCount,
        taskCount);
    assertTrue(
        String.format("no task with pid %d", PID),
        sample.getProcess().getReadingList().stream().anyMatch(stat -> stat.getTaskId() == PID));
  }

  // TODO(timur): this is too soft of an assertion, we'd like to provide a controlled time source
  private static void assertTimestamp(Instant timestamp, Instant start, Instant end) {
    String error = String.format("timestamp %s outside of range (%s -> %s)", timestamp, start, end);
    assertTrue(error, start.toEpochMilli() <= timestamp.toEpochMilli());
    assertTrue(error, timestamp.toEpochMilli() <= end.toEpochMilli());
  }
}
