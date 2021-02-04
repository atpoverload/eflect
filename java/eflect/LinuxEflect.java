package eflect;

import static eflect.util.ProcUtil.readProcStat;
import static eflect.util.ProcUtil.readTaskStats;

import eflect.data.EnergySample;
import eflect.data.async.AsyncProfilerSample;
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
public final class LinuxEflect extends Eflect {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final Duration asyncRate = Duration.ofMillis(2);

  private static boolean asyncRunning = false;

  private static synchronized String readAsyncProfiler() {
    if (!asyncRunning) {
      AsyncProfiler.getInstance().start(Events.CPU, asyncRate.getNano());
      asyncRunning = true;
    }
    AsyncProfiler.getInstance().stop();
    String traces = AsyncProfiler.getInstance().dumpRecords();
    AsyncProfiler.getInstance().resume(Events.CPU, asyncRate.getNano());
    return traces;
  }

  private static Collection<Supplier<?>> getSources() {
    Supplier<?> stat = () -> new ProcStatSample(Instant.now(), readProcStat());
    Supplier<?> task = () -> new ProcTaskSample(Instant.now(), readTaskStats());
    Supplier<?> rapl = () -> new EnergySample(Instant.now(), Rapl.getInstance().getEnergyStats());
    Supplier<?> async = () -> new AsyncProfilerSample(readAsyncProfiler());
    return List.of(stat, task, rapl, async);
  }

  public LinuxEflect(ScheduledExecutorService executor, Duration period) {
    super(
        getSources(),
        Rapl.getInstance().getSocketCount(),
        Rapl.getInstance().getWrapAroundEnergy(),
        cpu -> cpu / (CPU_COUNT / Rapl.getInstance().getSocketCount()),
        executor,
        period);
  }
}
