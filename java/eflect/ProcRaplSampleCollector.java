package eflect;

import clerk.storage.ClassMappedListStorage;
import clerk.util.FixedPeriodClerk;
import eflect.data.EnergySample;
import eflect.data.Sample;
import eflect.data.jiffies.ProcDataSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import jrapl.Rapl;

/** A clerk that collects jiffies and energy data for an intel proc system. */
public final class ProcRaplSampleCollector extends FixedPeriodClerk<Map<Class<?>, List<Sample>>> {
  private static final Supplier<Sample> raplSource =
      () -> new EnergySample(Instant.now(), Rapl.getInstance().getEnergyStats());

  public ProcRaplSampleCollector(ScheduledExecutorService executor, Duration period) {
    super(
        List.of(ProcDataSources::sampleProcStat, ProcDataSources::sampleTaskStats, raplSource),
        new ClassMappedListStorage<Sample, Map<Class<?>, List<Sample>>>() {
          @Override
          public Map<Class<?>, List<Sample>> process() {
            return getData();
          }
        },
        executor,
        period);
  }
}
