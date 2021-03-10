package eflect;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;

import clerk.FixedPeriodClerk;
import eflect.data.EnergySample;
import eflect.data.Writer;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import jrapl.Rapl;

/** A clerk that writes jiffies and energy data for an intel linux system. */
public final class LinuxWriter extends FixedPeriodClerk<Boolean> {
  private static Collection<Supplier<?>> getSources() {
    Supplier<?> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl = () -> new EnergySample(Instant.now(), Rapl.getInstance().getEnergyStats());
    return List.of(stat, task, rapl);
  }

  public LinuxWriter(String outputPath, ScheduledExecutorService executor, Duration period) {
    super(getSources(), new Writer(outputPath), executor, period);
  }
}
