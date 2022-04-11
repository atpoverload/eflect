package eflect;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.protos.sample.DataSet;
import eflect.protos.sample.Sample;
import eflect.sample.JiffiesDataSources;
import eflect.sample.RaplDataSources;
import eflect.util.CollectingFuture;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/** An unsafe interface for eflect in a linux system. */
public final class Eflect {
  private static final long PID = ProcessHandle.current().pid();
  private static final long DEFAULT_PERIOD_MS = 50;
  private static final AtomicInteger counter = new AtomicInteger();
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };

  private static ScheduledExecutorService executor;
  private static Eflect instance;

  /** Creates an instance of the underlying class if it hasn't been created yet. */
  public static synchronized Eflect getInstance() {
    if (instance == null) {
      instance = new Eflect();
    }
    return instance;
  }

  private final long periodMillis;
  private final DataSet.Builder dataSet = DataSet.newBuilder();
  private final ArrayList<CollectingFuture<? extends Sample>> data = new ArrayList<>();

  private Eflect() {
    this.periodMillis =
        Long.parseLong(
            System.getProperty("eflect.period.default", Long.toString(DEFAULT_PERIOD_MS)));
  }

  /** Creates and starts a new collector. If there is no executor, a new thread pool is spun-up. */
  public void start(long periodMillis) {
    // make sure the period is valid
    Duration period = Duration.ofMillis(periodMillis);
    if (period.equals(Duration.ZERO)) {
      throw new RuntimeException("cannot sample with a period of " + period);
    }

    // make sure we have an executor
    if (executor == null) {
      executor = newScheduledThreadPool(3, threadFactory);
    }

    // start a new collection
    dataSet.clear();
    data.clear();
    data.add(CollectingFuture.create(RaplDataSources::sampleRapl, period, executor));
    data.add(CollectingFuture.create(JiffiesDataSources::sampleCpus, period, executor));
    data.add(CollectingFuture.create(() -> JiffiesDataSources.sampleTasks(PID), period, executor));
  }

  /** Starts a collector with the default period. */
  public void start() {
    start(periodMillis);
  }

  /** Stops the collector. */
  public void stop() {
    data.stream().forEach(future -> future.cancel(true));
  }

  public DataSet read() {
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
              switch (sample.getDataCase()) {
                case CPU:
                  dataSet.addCpu(sample.getCpu());
                  break;
                case PROCESS:
                  dataSet.addProcess(sample.getProcess());
                  break;
                case RAPL:
                  dataSet.addRapl(sample.getRapl());
                  break;
                default:
                  break;
              }
            });
    data.clear();
    return dataSet.build();
  }

  /** Writes all sample data from the last session to the output directory. */
  public void dump(String outputPath) {
    try (FileOutputStream output = new FileOutputStream(outputPath)) {
      dataSet.build().writeTo(output);
    } catch (Exception e) {
      System.out.println("unable to write data");
      e.printStackTrace();
    }
  }

  /** Shutdown the executor. */
  public void shutdown() {
    executor.shutdown();
    executor = null;
  }
}
