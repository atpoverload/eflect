package eflect.testing;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.protos.sample.Sample;
import eflect.sample.JiffiesDataSources;
import eflect.sample.SampleCollector;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** A clerk that collects jiffies and energy data for an intel proc system. */
public final class FakeEflect extends SampleCollector {
  private static List<Supplier<? extends Sample>> getSources() {
    return List.of(JiffiesDataSources::sampleCpus, JiffiesDataSources::sampleTasks);
  }

  public FakeEflect(ScheduledExecutorService executor, Duration period) {
    super(getSources(), executor, period);
  }

  public static void main(String[] args) throws Exception {
    final AtomicInteger counter = new AtomicInteger();
    ScheduledExecutorService executor =
        newScheduledThreadPool(4, r -> new Thread(r, "eflect-" + counter.getAndIncrement()));

    FakeEflect collector = new FakeEflect(executor, Duration.ofMillis(100));
    collector.start();

    for (int i = 0; i < 10; i++) {
      Thread.sleep(100);
    }

    collector.stop();

    System.out.println(collector.read());

    executor.shutdown();
  }
}
