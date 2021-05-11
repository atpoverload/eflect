package eflect;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;

import eflect.data.EnergySample;
import eflect.data.async.AsyncProfilerSample;
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
import one.profiler.AsyncProfiler;
import one.profiler.Events;

/** A Clerk that estimates the energy consumed by an application on an intel linux system. */
public final class LinuxRaplEflect extends Eflect {
  // data sources
  private static Collection<Supplier<?>> getSources() {
    Supplier<?> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl = () -> new EnergySample(Instant.now(), Rapl.getInstance().getEnergyStats());
    Supplier<?> async = () -> new AsyncProfilerSample(Instant.now(), readAsyncProfiler());
    return List.of(stat, task, rapl, async);
  }

  // system constants
  private static final int SOCKET_COUNT = Rapl.getInstance().getSocketCount();
  // TODO(timur): how do we get the real value properly?
  private static final int COMPONENT_COUNT = 3;
  private static final double WRAP_AROUND_ENERGY = Rapl.getInstance().getWrapAroundEnergy();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  // async helper code
  private static final Duration asyncPeriod =
      Duration.ofMillis(Integer.parseInt(System.getProperty("eflect.async.period", "2")));
  private static final Duration asyncCollectionPeriod =
      Duration.ofMillis(
          Integer.parseInt(System.getProperty("eflect.async.collection.period", "500")));

  private static boolean asyncRunning = false;
  private static Instant last = Instant.now();

  private static synchronized String readAsyncProfiler() {
    Instant now = Instant.now();
    if (Duration.between(last, now).toMillis() > asyncCollectionPeriod.toMillis()) {
      if (!asyncRunning) {
        AsyncProfiler.getInstance().start(Events.CPU, asyncPeriod.getNano());
        asyncRunning = true;
      }
      AsyncProfiler.getInstance().stop();
      String traces = AsyncProfiler.getInstance().dumpRecords();
      AsyncProfiler.getInstance().resume(Events.CPU, asyncPeriod.getNano());
      last = now;
      return traces;
    }
    return "0,-1,dummy\n";
  }

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
