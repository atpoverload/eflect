package eflect;

import eflect.data.Sample;
import eflect.data.SampleCollector;
import eflect.data.async.AsyncProfilerDataSources;
import eflect.data.jiffies.ProcDataSources;
import eflect.data.rapl.RaplDataSources;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that collects jiffies and energy data for an intel proc system. */
public final class EflectSampleCollector extends SampleCollector {
  private static List<Supplier<? extends Sample>> getSources() {
    return List.of(
        ProcDataSources::sampleProcStat,
        ProcDataSources::sampleTaskStats,
        RaplDataSources::sampleRapl,
        AsyncProfilerDataSources::sampleAsyncProfiler);
  }

  public EflectSampleCollector(ScheduledExecutorService executor, Duration period) {
    super(getSources(), executor, period);
  }
}
