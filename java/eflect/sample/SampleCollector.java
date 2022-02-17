package eflect.sample;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import eflect.protos.sample.DataSet;
import eflect.protos.sample.Sample;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/** A clerk that collects samples into a data set. */
public final class SampleCollector {
  private final ArrayList<Future<Sample>> sampleFutures = new ArrayList<>();
  private final ArrayList<Sample> data = new ArrayList<>();

  private final ScheduledExecutorService executor;

  private boolean isRunning;

  public SampleCollector(ScheduledExecutorService executor) {
    this.executor = executor;
  }

  /** Starts collector from each source. */
  public final void start(Supplier<Sample> source, Duration period) {
    isRunning = true;
    sampleFutures.add(executor.submit(() -> collectAndReschedule(source, period)));
  }

  /** Stops all collection. */
  public final void stop() {
    isRunning = false;
  }

  /** Return the processor's output. */
  public final DataSet read() {
    // grab any existing futures
    synchronized (sampleFutures) {
      for (Future<Sample> sampleFuture : sampleFutures) {
        if (sampleFuture.isDone()) {
          try {
            data.add(sampleFuture.get());
          } catch (Exception e) {
            System.out.println("could not consume a future");
            e.printStackTrace();
          }
        }
      }
    }
    sampleFutures.clear();
    // build a new data set
    DataSet.Builder dataSet = DataSet.newBuilder();
    data.forEach(
        sample -> {
          switch (sample.getDataCase()) {
            case CPU:
              dataSet.addCpu(sample.getCpu());
              break;
            case RAPL:
              dataSet.addRapl(sample.getRapl());
              break;
            case TASK:
              dataSet.addTask(sample.getTask());
              break;
            default:
              break;
          }
        });
    return dataSet.build();
  }

  /** Collects a sample and then re-schedules it for the next period start. */
  private Sample collectAndReschedule(Supplier<Sample> source, Duration period) {
    if (!isRunning) {
      return Sample.getDefaultInstance();
    }

    Instant start = Instant.now();
    Sample sample = source.get();
    Duration rescheduleTime = period.minus(Duration.between(start, Instant.now()));

    synchronized (sampleFutures) {
      if (rescheduleTime.toNanos() > 0) {
        // if we have some extra time, schedule the next one in the future
        sampleFutures.add(
            executor.schedule(
                () -> collectAndReschedule(source, period), rescheduleTime.toNanos(), NANOSECONDS));
      } else {
        // if we don't, run the next one immediately
        sampleFutures.add(executor.submit(() -> collectAndReschedule(source, period)));
      }
    }
    return sample;
  }
}
