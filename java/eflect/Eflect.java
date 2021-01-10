package eflect;

import static eflect.util.AsyncProfilerUtil.readAsyncProfiler;
import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;
import static jrapl.Rapl.SOCKET_COUNT;
import static jrapl.Rapl.WRAP_AROUND_ENERGY;
import static jrapl.Rapl.loadRapl;
import static jrapl.Rapl.getEnergyStats;

import clerk.FixedPeriodClerk;
import eflect.data.Accountant;
import eflect.data.AccountantMerger;
import eflect.data.EnergyAccountant;
import eflect.data.EnergyFootprint;
import eflect.data.EnergySample;
import eflect.data.StackTraceAligner;
import eflect.data.async.AsyncProfilerSample;
import eflect.data.jiffies.JiffiesAccountant;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/** A profiler that estimates the energy consumed by the current application. */
public final class Eflect extends FixedPeriodClerk<Collection<EnergyFootprint>> {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private static Collection<Supplier<?>> getSources() {
    loadRapl("/external/eflect/java/eflect/");
    Supplier<?> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl = () -> new EnergySample(Instant.now(), getEnergyStats());
    Supplier<?> async = () -> new AsyncProfilerSample(readAsyncProfiler());
    return List.of(stat, task, rapl, async);
  }

  public Eflect(Duration period) {
    super(
        getSources(),
        new StackTraceAligner(
            new AccountantMerger<EnergyFootprint>() {
              @Override
              public Accountant<Collection<EnergyFootprint>> newAccountant() {
                return new EnergyAccountant(
                    SOCKET_COUNT,
                    WRAP_AROUND_ENERGY,
                    new JiffiesAccountant(SOCKET_COUNT, cpu -> cpu / (CPU_COUNT / SOCKET_COUNT)));
              }
            }),
        period);
  }
}
