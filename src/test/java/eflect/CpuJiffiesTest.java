package eflect;

import static org.junit.Assert.assertEquals;

import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import org.junit.Test;

public class CpuJiffiesTest {
  @Test
  public void difference_reading() {
    CpuReading diff =
        CpuJiffies.difference(
            CpuReading.newBuilder().setCpu(0).build(),
            CpuReading.newBuilder().setCpu(0).setUser(1).build());

    assertEquals(0, diff.getCpu());
    assertEquals(1, diff.getUser());
  }

  @Test
  public void difference_sample() {
    CpuDifference diff =
        CpuJiffies.difference(
            CpuSample.newBuilder()
                .setTimestamp(Timestamps.EPOCH)
                .addReading(CpuReading.newBuilder().setCpu(0))
                .build(),
            CpuSample.newBuilder()
                .setTimestamp(Timestamps.add(Timestamps.EPOCH, Durations.fromMillis(1)))
                .addReading(CpuReading.newBuilder().setCpu(0).setUser(1))
                .build());

    assertEquals(Timestamps.EPOCH, diff.getStart());
    assertEquals(Timestamps.add(Timestamps.EPOCH, Durations.fromMillis(1)), diff.getEnd());
    assertEquals(1, diff.getReadingCount());
    assertEquals(0, diff.getReading(0).getCpu());
    assertEquals(1, diff.getReading(0).getUser());
  }
}
