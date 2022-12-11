package eflect.testing;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import com.google.protobuf.util.Timestamps;
import eflect.EflectDataSet;
import eflect.sample.CpuSample;
import eflect.sample.Jiffies;
import eflect.sample.SamplingFuture;
import eflect.sample.TaskSample;
import eflect.virtualization.RaplVirtualizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import jrapl.RaplReading;
import jrapl.RaplSample;

/** A singleton for local eflect sampling in a linux system. */
public final class FakeEflect {
  private static final long PID = ProcessHandle.current().pid();
  private static final Duration DEFAULT_PERIOD_MS = Duration.ofMillis(10);
  private static final AtomicInteger COUNTER = new AtomicInteger();

  private final ScheduledExecutorService executor = newScheduledThreadPool(3);
  private final EflectDataSet.Builder dataSet = EflectDataSet.newBuilder();
  private final ArrayList<SamplingFuture<?>> data = new ArrayList<>();

  private FakeEflect() {}

  /** Creates and starts a new collector. If there is no executor, a new thread pool is spun-up. */
  public void start() {
    dataSet.clear();
    data.clear();
    data.add(
        SamplingFuture.fixedPeriod(
            () ->
                RaplSample.newBuilder()
                    .setTimestamp(Timestamps.fromMillis(Instant.now().toEpochMilli()))
                    .addReading(
                        RaplReading.newBuilder().setSocket(0).setPackage(COUNTER.getAndIncrement()))
                    .build(),
            DEFAULT_PERIOD_MS,
            executor));
    data.add(SamplingFuture.fixedPeriod(Jiffies::sampleCpus, DEFAULT_PERIOD_MS, executor));
    data.add(
        SamplingFuture.fixedPeriod(() -> Jiffies.sampleTasks(PID), DEFAULT_PERIOD_MS, executor));
  }

  /** Stops the collection. */
  public void stop() {
    data.stream().forEach(future -> future.cancel(true));
  }

  /** Returns the data from the last session. */
  public EflectDataSet read() {
    data.stream()
        .flatMap(
            future -> {
              try {
                return future.get().stream();
              } catch (Exception e) {
                return Stream.empty();
              }
            })
        .forEach(
            sample -> {
              if (sample instanceof RaplSample) {
                dataSet.addRapl((RaplSample) sample);
              } else if (sample instanceof CpuSample) {
                dataSet.addCpu((CpuSample) sample);
              } else if (sample instanceof TaskSample) {
                dataSet.addTask((TaskSample) sample);
              }
            });
    data.clear();
    return dataSet.build();
  }

  public static void main(String[] args) throws Exception {
    FakeEflect eflect = new FakeEflect();
    eflect.start();
    Thread.sleep(100 * DEFAULT_PERIOD_MS.toMillis());
    eflect.stop();

    List<?> data =
        RaplVirtualizer.virtualize(eflect.read(), cpu -> 1, DEFAULT_PERIOD_MS.toMillis());
    System.out.println(data);

    eflect.executor.shutdown();
  }
}
