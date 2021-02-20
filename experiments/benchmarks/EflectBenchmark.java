package eflect.benchmarks;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;

public class EflectBenchmark {
  @Benchmark
  public void doNothing() {}

  @Benchmark
  public void sleep() throws Exception {
    TimeUnit.MICROSECONDS.sleep(100);
  }

  @Benchmark
  public double logPi() {
    return Math.log(Math.PI);
  }
}
