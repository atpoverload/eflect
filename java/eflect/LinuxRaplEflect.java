package eflect;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;

import eflect.data.EnergySample;
import eflect.data.jiffies.JiffiesAccountant;
import eflect.data.jiffies.ProcStatSample;
import eflect.data.jiffies.ProcTaskSample;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import jrapl.Rapl;

/** A Clerk that estimates the energy consumed by an application on an intel linux system. */
public final class LinuxRaplEflect extends Eflect {
  // data sources
  private static Collection<Supplier<?>> getSources() {
    Supplier<?> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl = () -> new EnergySample(Instant.now(), Rapl.getInstance().getEnergyStats());
    return List.of(stat, task, rapl);
  }

  // system constants
  private static final int SOCKET_COUNT = Rapl.getInstance().getSocketCount();
  // TODO(timur): how do we get the real value properly?
  private static final int COMPONENT_COUNT = 3;
  private static final double WRAP_AROUND_ENERGY = Rapl.getInstance().getWrapAroundEnergy();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  public LinuxRaplEflect(ScheduledExecutorService executor, Duration period) {
    super(
        getSources(),
        SOCKET_COUNT,
        COMPONENT_COUNT,
        WRAP_AROUND_ENERGY,
        () -> new JiffiesAccountant(SOCKET_COUNT, cpu -> cpu / (CPU_COUNT / SOCKET_COUNT)),
        100,
        executor,
        period);
  }
}
