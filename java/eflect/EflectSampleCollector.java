package eflect;

import clerk.storage.ClassMappedListStorage;
import clerk.util.FixedPeriodClerk;
import eflect.data.EnergySample;
import eflect.data.EnergySample.Component;
import eflect.data.Sample;
import eflect.data.async.AsyncProfilerDataSources;
import eflect.data.jiffies.ProcDataSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import jrapl.Rapl;

/** A clerk that collects jiffies and energy data for an intel proc system. */
public final class EflectSampleCollector extends FixedPeriodClerk<Map<Class<?>, List<Sample>>> {
  private static final Sample sampleRapl() {
    double[][] sample = Rapl.getInstance().getEnergyStats();
    double[][] energy = new double[sample.length][4];
    for (int socket = 0; socket < sample.length; socket++) {
      energy[socket][Component.CPU] = sample[socket][Component.CPU];
      energy[socket][Component.DRAM] = sample[socket][Component.DRAM];
      energy[socket][Component.PACKAGE] = sample[socket][Component.PACKAGE];
      energy[socket][Component.GPU] = 0;
    }
    return new EnergySample(Instant.now(), energy);
  }

  private static List<Supplier<Sample>> getSources() {
    return List.of(
        ProcDataSources::sampleProcStat,
        ProcDataSources::sampleTaskStats,
        EflectSampleCollector::sampleRapl,
        AsyncProfilerDataSources::sampleAsyncProfiler);
  }

  public EflectSampleCollector(ScheduledExecutorService executor, Duration period) {
    super(
        getSources(),
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
