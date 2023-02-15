package eflect;

import static com.google.protobuf.util.Timestamps.EPOCH;
import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.logging.Handler;
import jrapl.RaplReading;
import jrapl.RaplSample;
import org.junit.Before;
import org.junit.Test;

public class RaplVirtualizerTest {
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
    assertEmpty(RaplVirtualizer.virtualize(emptyList(), emptyList(), BUCKET_SIZE));
  }

  @Test
  public void virtualize_notEnoughSamples_emptyList() {
    for (int i = 1; i <= 3; i++) {
      assertEmpty(
          RaplVirtualizer.virtualize(
              nCopies(i, RaplSample.getDefaultInstance()), emptyList(), BUCKET_SIZE));
      assertEmpty(
          RaplVirtualizer.virtualize(
              nCopies(i, RaplSample.getDefaultInstance()),
              List.of(Virtualization.getDefaultInstance()),
              BUCKET_SIZE));
      assertEmpty(
          RaplVirtualizer.virtualize(
              emptyList(), nCopies(i, Virtualization.getDefaultInstance()), BUCKET_SIZE));
      assertEmpty(
          RaplVirtualizer.virtualize(
              List.of(RaplSample.getDefaultInstance()),
              nCopies(i, Virtualization.getDefaultInstance()),
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
                        .setUnit(Virtualization.VirtualizedComponent.Unit.ENERGY)
                        .setValue(1))
                .build()),
        RaplVirtualizer.virtualize(
            List.of(
                RaplSample.newBuilder()
                    .setTimestamp(EPOCH)
                    .addReading(RaplReading.newBuilder().setSocket(0).setPackage(0))
                    .build(),
                RaplSample.newBuilder()
                    .setTimestamp(fromMillis(BUCKET_SIZE))
                    .addReading(RaplReading.newBuilder().setSocket(0).setPackage(1))
                    .build()),
            List.of(
                Virtualization.newBuilder()
                    .setStart(EPOCH)
                    .setEnd(fromMillis(BUCKET_SIZE))
                    .addVirtualization(
                        Virtualization.VirtualizedComponent.newBuilder()
                            .setTaskId(ID)
                            .setComponent(
                                Virtualization.VirtualizedComponent.Component.newBuilder()
                                    .setCpu(0))
                            .setUnit(Virtualization.VirtualizedComponent.Unit.ACTIVITY)
                            .setValue(1))
                    .build()),
            BUCKET_SIZE));
  }

  @Test
  public void virtualize_multipleTasks_alignable() {
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
                        .setUnit(Virtualization.VirtualizedComponent.Unit.ENERGY)
                        .setValue(0.5))
                .addVirtualization(
                    Virtualization.VirtualizedComponent.newBuilder()
                        .setTaskId(ID + 1)
                        .setComponent(
                            Virtualization.VirtualizedComponent.Component.newBuilder().setCpu(0))
                        .setUnit(Virtualization.VirtualizedComponent.Unit.ENERGY)
                        .setValue(0.5))
                .build()),
        RaplVirtualizer.virtualize(
            List.of(
                RaplSample.newBuilder()
                    .setTimestamp(EPOCH)
                    .addReading(RaplReading.newBuilder().setSocket(0).setPackage(0))
                    .build(),
                RaplSample.newBuilder()
                    .setTimestamp(fromMillis(BUCKET_SIZE))
                    .addReading(RaplReading.newBuilder().setSocket(0).setPackage(1))
                    .build()),
            List.of(
                Virtualization.newBuilder()
                    .setStart(EPOCH)
                    .setEnd(fromMillis(BUCKET_SIZE))
                    .addVirtualization(
                        Virtualization.VirtualizedComponent.newBuilder()
                            .setTaskId(ID)
                            .setComponent(
                                Virtualization.VirtualizedComponent.Component.newBuilder()
                                    .setCpu(0))
                            .setUnit(Virtualization.VirtualizedComponent.Unit.ACTIVITY)
                            .setValue(0.5))
                    .addVirtualization(
                        Virtualization.VirtualizedComponent.newBuilder()
                            .setTaskId(ID + 1)
                            .setComponent(
                                Virtualization.VirtualizedComponent.Component.newBuilder()
                                    .setCpu(0))
                            .setUnit(Virtualization.VirtualizedComponent.Unit.ACTIVITY)
                            .setValue(0.5))
                    .build()),
            BUCKET_SIZE));
  }

  private static <T> void assertEmpty(List<T> l) {
    assertEquals(0, l.size());
  }
}
