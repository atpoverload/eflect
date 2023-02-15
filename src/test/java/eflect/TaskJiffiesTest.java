package eflect;

import static org.junit.Assert.assertEquals;

import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import org.junit.Test;

public class TaskJiffiesTest {
  private static final int ID = 45888;  // some random 5 digit number i generated

  @Test
  public void difference_reading() {
    TaskReading diff =
        TaskJiffies.difference(
            TaskReading.newBuilder().setTaskId(ID).setCpu(0).build(),
            TaskReading.newBuilder().setTaskId(ID).setCpu(0).setUser(1).build());

    assertEquals(diff.getCpu(), 0);
    assertEquals(diff.getUser(), 1);
  }

  @Test
  public void difference_sample() {
    TaskDifference diff =
        TaskJiffies.difference(
            TaskSample.newBuilder()
                .setTimestamp(Timestamps.EPOCH)
                .addReading(TaskReading.newBuilder().setTaskId(ID).setCpu(0))
                .build(),
            TaskSample.newBuilder()
                .setTimestamp(Timestamps.add(Timestamps.EPOCH, Durations.fromMillis(1)))
                .addReading(TaskReading.newBuilder().setTaskId(ID).setCpu(0).setUser(1))
                .build());

    assertEquals(Timestamps.EPOCH, diff.getStart());
    assertEquals(Timestamps.add(Timestamps.EPOCH, Durations.fromMillis(1)), diff.getEnd());
    assertEquals(1, diff.getReadingCount());
    assertEquals(ID, diff.getReading(0).getTaskId());
    assertEquals(0, diff.getReading(0).getCpu());
    assertEquals(1, diff.getReading(0).getUser());
  }
}
