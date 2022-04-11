package eflect.sample;

import static org.junit.Assert.assertTrue;

import eflect.protos.sample.Sample;
import java.time.Instant;
import org.junit.Test;

public class RaplDataSourcesTest {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final long PID = ProcessHandle.current().pid();

  @Test
  public void procStat_smokeTest() {
    Instant start = Instant.now();
    Sample sample = RaplDataSources.sampleRapl();
    Instant end = Instant.now();

    assertTrue(String.format("expected RAPL but got %s", sample.getDataCase()), sample.hasRapl());
    assertTimestamp(Instant.ofEpochMilli(sample.getRapl().getTimestamp()), start, end);
    int socketCount = sample.getRapl().getReadingCount();
    assertTrue(
        String.format("expected at least one socket but got %d", socketCount), 0 < socketCount);
  }

  // TODO(timur): this is too soft of an assertion, we'd like to provide a controlled time source
  private static void assertTimestamp(Instant timestamp, Instant start, Instant end) {
    String error = String.format("timestamp %s outside of range (%s -> %s)", timestamp, start, end);
    assertTrue(error, start.toEpochMilli() <= timestamp.toEpochMilli());
    assertTrue(error, timestamp.toEpochMilli() <= end.toEpochMilli());
  }
}
