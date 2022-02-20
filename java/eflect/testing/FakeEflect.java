package eflect.testing;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.protos.sample.DataSet;
import eflect.protos.sample.Sample;
import eflect.sample.JiffiesDataSources;
import eflect.sample.SampleCollector;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** A clerk that collects jiffies and energy data for an intel proc system. */
class FakeEflect {
  private final SampleCollector collector;

  private static List<Supplier<? extends Sample>> getSources() {
    return List.of(JiffiesDataSources::sampleCpuStats, JiffiesDataSources::sampleTaskStats);
  }

  private FakeEflect(ScheduledExecutorService executor) {
    this.collector = new SampleCollector(executor);
  }

  public void start() {
    for (Supplier<? extends Sample> source : getSources()) {
      collector.start(source, Duration.ofMillis(100));
    }
  }

  public void stop() {
    collector.stop();
  }

  public DataSet read() {
    return collector.read();
  }

  public static void main(String[] args) throws Exception {
    final AtomicInteger counter = new AtomicInteger();
    ScheduledExecutorService executor =
        newScheduledThreadPool(4, r -> new Thread(r, "eflect-" + counter.getAndIncrement()));

    FakeEflect collector = new FakeEflect(executor);
    collector.start();

    for (int i = 0; i < 10; i++) {
      Thread.sleep(100);
    }

    collector.stop();

    System.out.println(collector.read());

    executor.shutdown();
  }
}
