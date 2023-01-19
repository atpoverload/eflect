package eflect.virtualization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.util.Timestamps;
import eflect.EflectDataSet;
import eflect.sample.CpuReading;
import eflect.sample.CpuSample;
import eflect.sample.TaskReading;
import eflect.sample.TaskSample;
import java.time.Instant;
import java.util.List;
import org.junit.Test;

// TODO: build goldens for these tests
public class JiffiesVirtualizerTest {
    @Test
    public void processCpus() {
        List<Difference<CpuReading>> diffs = JiffiesVirtualizer.processCpus(List.of(
            CpuSample.newBuilder()
                .setTimestamp(Timestamps.fromMillis(0))
                .addReading(CpuReading.newBuilder().setCpu(0).setUser(0))
                .build(),
            CpuSample.newBuilder()
                .setTimestamp(Timestamps.fromMillis(1))
                .addReading(CpuReading.newBuilder().setCpu(0).setUser(1))
                .build()
            ));

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).data.size(), 1);
        assertEquals(diffs.get(0).data.get(0).getCpu(), 0);
        assertEquals(diffs.get(0).data.get(0).getUser(), 1);
    }

    @Test
    public void processTasks() {
        List<Difference<TaskReading>> diffs = JiffiesVirtualizer.processTasks(List.of(
            TaskSample.newBuilder()
                .setTimestamp(Timestamps.fromMillis(0))
                .addReading(TaskReading.newBuilder().setCpu(0).setUser(0))
                .build(),
            TaskSample.newBuilder()
                .setTimestamp(Timestamps.fromMillis(1))
                .addReading(TaskReading.newBuilder().setCpu(0).setUser(1))
                .build()
            ));

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).data.size(), 1);
        assertEquals(diffs.get(0).data.get(0).getCpu(), 0);
        assertEquals(diffs.get(0).data.get(0).getUser(), 1);
    }

    @Test
    public void virtualize_noSamples_emptyArray() {
        assertTrue(JiffiesVirtualizer.virtualize(EflectDataSet.getDefaultInstance(), 0).isEmpty());
    }

    @Test
    public void virtualize_singleSample_emptyArray() {
        assertTrue(JiffiesVirtualizer.virtualize(
            EflectDataSet.newBuilder().addCpu(CpuSample.getDefaultInstance()).build(), 0).isEmpty());
    }
}
