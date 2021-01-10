package eflect.jmh;

import eflect.testing.DummyEflect;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.profile.ExternalProfiler;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** A profiler that reports the number of samples collected by a clerk. */
public final class EflectProfiler implements ExternalProfiler {
  private static final int period = Integer.parseInt(System.getProperty("eflect.jmh.period", "41"));

  private DummyEflect eflect;

  public EflectProfiler() {}

  /** Starts collecting data. */
  @Override
  public final void beforeTrial(BenchmarkParams benchmarkParams) {
    eflect = new DummyEflect(Duration.ofMillis(period));
    eflect.start();
  }

  /** Stops collecting data and packages it in a result. */
  @Override
  public final Collection<? extends Result> afterTrial(
      BenchmarkResult br, long pid, File stdOut, File stdErr) {
    eflect.stop();
    return new EflectResult(
        eflect.read(), br.getMetadata().getWarmupOps() + br.getMetadata().getMeasurementOps());
  }

  // dummy implementations for things we don't use
  @Override
  public String getDescription() {
    return "eflect";
  }

  @Override
  public Collection<String> addJVMInvokeOptions(BenchmarkParams params) {
    return new ArrayList<String>();
  }

  @Override
  public Collection<String> addJVMOptions(BenchmarkParams params) {
    return new ArrayList<String>();
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
