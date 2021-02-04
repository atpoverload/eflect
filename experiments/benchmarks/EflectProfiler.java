package eflect.benchmarks;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.LinuxEflect;
import eflect.data.EnergyFootprint;
import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.profile.ExternalProfiler;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** A profiler that reports the total amount of energy consumed by the benchmark. */
public final class EflectProfiler implements ExternalProfiler {
  private static final AtomicInteger counter = new AtomicInteger(0);
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };
  private static final ScheduledExecutorService executor = newScheduledThreadPool(5, threadFactory);

  private static Collection<EflectResult> sum(
      Collection<EnergyFootprint> footprints, BenchmarkResult result) {
    double energy = 0;
    for (EnergyFootprint footprint : footprints) {
      energy += footprint.energy;
    }
    return List.of(
        new EflectResult(new double[] {energy / br.getPrimaryResult().getStatistics().getN()}));
  }

  private LinuxEflect eflect;

  /** Starts eflect. */
  @Override
  public final void beforeTrial(BenchmarkParams benchmarkParams) {
    eflect = new LinuxEflect(executor, Duration.ofMillis(41));
    eflect.start();
  }

  /** Stops eflect and transforms the data into an {@link EflectResult}. */
  @Override
  public final Collection<? extends Result> afterTrial(
      BenchmarkResult br, long pid, File stdOut, File stdErr) {
    eflect.stop();
    Collection<EnergyFootprint> footprints = eflect.read();
    eflect = null;
    return sum(footprints, br);
  }

  @Override
  public String getDescription() {
    return "eflect";
  }

  @Override
  public Collection<String> addJVMInvokeOptions(BenchmarkParams params) {
    return Collections.emptyList();
  }

  @Override
  public Collection<String> addJVMOptions(BenchmarkParams params) {
    return Collections.emptyList();
  }

  @Override
  public boolean allowPrintOut() {
    return true;
  }

  @Override
  public boolean allowPrintErr() {
    return false;
  }

  // driver for this profiler
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().addProfiler(EflectProfiler.class).build();
    new Runner(opt).run();
  }
}
