package eflect;

import static com.google.protobuf.util.Timestamps.EPOCH;
import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.logging.Handler;
import org.junit.Before;
import org.junit.Test;

public class JiffiesVirtualizerTest {
  private static final int ID = 45888; // some random 5 digit number i generated
  private static final int BUCKET_SIZE = 10;

  @Before
  public void disableLogging() {
    // disable logging for the test
    for (Handler hdlr : LoggerUtil.getLogger().getHandlers()) {
      LoggerUtil.getLogger().removeHandler(hdlr);
    }
  }

  @Test
  public void virtualize_emptyLists_emptyList() {
    assertEmpty(JiffiesVirtualizer.virtualize(emptyList(), emptyList(), BUCKET_SIZE));
  }

  @Test
  public void virtualize_notEnoughSamples_emptyList() {
    for (int i = 1; i <= 3; i++) {
      assertEmpty(
          JiffiesVirtualizer.virtualize(
              nCopies(i, CpuSample.getDefaultInstance()), emptyList(), BUCKET_SIZE));
      assertEmpty(
          JiffiesVirtualizer.virtualize(
              nCopies(i, CpuSample.getDefaultInstance()),
              List.of(TaskSample.getDefaultInstance()),
              BUCKET_SIZE));
      assertEmpty(
          JiffiesVirtualizer.virtualize(
              emptyList(), nCopies(i, TaskSample.getDefaultInstance()), BUCKET_SIZE));
      assertEmpty(
          JiffiesVirtualizer.virtualize(
              List.of(CpuSample.getDefaultInstance()),
              nCopies(i, TaskSample.getDefaultInstance()),
              1));
    }
  }

  @Test
  public void virtualize_singleTask_alignable() {
    assertEquals(
        List.of(
            Virtualization.newBuilder()
                .setStart(EPOCH)
                .setEnd(fromMillis(BUCKET_SIZE))
                .addVirtualization(
                    Virtualization.VirtualizedComponent.newBuilder()
                        .setTaskId(ID)
                        .setComponent(
                            Virtualization.VirtualizedComponent.Component.newBuilder().setCpu(0))
                        .setUnit(Virtualization.VirtualizedComponent.Unit.ACTIVITY)
                        .setValue(1))
                .build()),
        JiffiesVirtualizer.virtualize(
            List.of(
                CpuSample.newBuilder()
                    .setTimestamp(EPOCH)
                    .addReading(CpuReading.newBuilder().setCpu(0).setUser(0))
                    .build(),
                CpuSample.newBuilder()
                    .setTimestamp(fromMillis(BUCKET_SIZE))
                    .addReading(CpuReading.newBuilder().setCpu(0).setUser(1))
                    .build()),
            List.of(
                TaskSample.newBuilder()
                    .setTimestamp(EPOCH)
                    .addReading(TaskReading.newBuilder().setTaskId(ID).setCpu(0).setUser(0))
                    .build(),
                TaskSample.newBuilder()
                    .setTimestamp(fromMillis(BUCKET_SIZE))
                    .addReading(TaskReading.newBuilder().setTaskId(ID).setCpu(0).setUser(1))
                    .build()),
            BUCKET_SIZE));
  }

  @Test
  public void virtualize_multipleTask_alignable() {
    assertEquals(
        List.of(
            Virtualization.newBuilder()
                .setStart(EPOCH)
                .setEnd(fromMillis(BUCKET_SIZE))
                .addVirtualization(
                    Virtualization.VirtualizedComponent.newBuilder()
                        .setTaskId(ID)
                        .setComponent(
                            Virtualization.VirtualizedComponent.Component.newBuilder().setCpu(0))
                        .setUnit(Virtualization.VirtualizedComponent.Unit.ACTIVITY)
                        .setValue(0.5))
                .addVirtualization(
                    Virtualization.VirtualizedComponent.newBuilder()
                        .setTaskId(ID + 1)
                        .setComponent(
                            Virtualization.VirtualizedComponent.Component.newBuilder().setCpu(0))
                        .setUnit(Virtualization.VirtualizedComponent.Unit.ACTIVITY)
                        .setValue(0.5))
                .build()),
        JiffiesVirtualizer.virtualize(
            List.of(
                CpuSample.newBuilder()
                    .setTimestamp(EPOCH)
                    .addReading(CpuReading.newBuilder().setCpu(0).setUser(0))
                    .build(),
                CpuSample.newBuilder()
                    .setTimestamp(fromMillis(BUCKET_SIZE))
                    .addReading(CpuReading.newBuilder().setCpu(0).setUser(2))
                    .build()),
            List.of(
                TaskSample.newBuilder()
                    .setTimestamp(EPOCH)
                    .addReading(TaskReading.newBuilder().setTaskId(ID).setCpu(0).setUser(0))
                    .addReading(TaskReading.newBuilder().setTaskId(ID + 1).setCpu(0).setUser(0))
                    .build(),
                TaskSample.newBuilder()
                    .setTimestamp(fromMillis(BUCKET_SIZE))
                    .addReading(TaskReading.newBuilder().setTaskId(ID).setCpu(0).setUser(1))
                    .addReading(TaskReading.newBuilder().setTaskId(ID + 1).setCpu(0).setUser(1))
                    .build()),
            BUCKET_SIZE));
  }

  private static <T> void assertEmpty(List<T> l) {
    assertEquals(0, l.size());
  }
}
