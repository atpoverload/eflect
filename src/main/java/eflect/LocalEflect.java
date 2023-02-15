package eflect;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import java.io.FileOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jrapl.Powercap;
import jrapl.RaplSample;

/** A thread-safe singleton for local eflect sampling in a linux system. */
public final class LocalEflect {
  // TODO(timur): we should look up the minimum possible sampling rate (i.e. change in values)
  private static final long DEFAULT_PERIOD_MS = 10;
  private static final AtomicInteger counter = new AtomicInteger();
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };

  private static ScheduledExecutorService executor;
  private static LocalEflect instance;

  private static Logger logger = LoggerUtil.getLogger();

  /** Writes data to an output path. */
  public void dump(EflectDataSet dataSet, String outputPath) {
    try (FileOutputStream output = new FileOutputStream(outputPath)) {
      dataSet.writeTo(output);
    } catch (Exception e) {
      logger.info("unable to write data: " + e.getMessage());
    }
  }

  /** Creates an instance of the underlying class if it hasn't been created yet. */
  public static synchronized LocalEflect getInstance() {
    if (instance == null) {
      instance = new LocalEflect();
    }
    return instance;
  }

  private final long periodMillis;
  private final EflectDataSet.Builder dataSet = EflectDataSet.newBuilder();
  private final ArrayList<SamplingFuture<?>> data = new ArrayList<>();

  private boolean isRunning = false;

  private LocalEflect() {
    this.periodMillis =
        Long.parseLong(
            System.getProperty("eflect.period.default", Long.toString(DEFAULT_PERIOD_MS)));
  }

  /** Creates and starts a new collector. If there is no executor, a new thread pool is spun-up. */
  public void start(long periodMillis) {
    synchronized (this) {
      if (isRunning) {
        logger.info("ignoring start request while sampling");
        return;
      }
      // make sure the period is valid
      Duration period = Duration.ofMillis(periodMillis);
      if (period.equals(Duration.ZERO)) {
        throw new RuntimeException("cannot sample with a period of " + period);
      }

      // make sure we have an executor
      if (executor == null) {
        logger.info("creating a new executor");
        executor = newScheduledThreadPool(3, threadFactory);
      }

      // start a new collection
      dataSet.clear();
      data.clear();
      data.add(SamplingFuture.fixedPeriod(Powercap::sample, period, executor));
      data.add(SamplingFuture.fixedPeriod(CpuJiffies::sample, period, executor));
      data.add(SamplingFuture.fixedPeriod(TaskJiffies::sampleSelf, period, executor));
      logger.info("started sampling at " + periodMillis + " ms");
    }
  }

  /** Starts a collector with the default period. */
  public void start() {
    start(periodMillis);
  }

  /** Stops the collection. */
  public void stop() {
    synchronized (this) {
      if (isRunning) {
        logger.info("ignoring stop request while not sampling");
        return;
      }
      data.stream().forEach(future -> future.cancel(true));
      logger.info("stopped sampling");
    }
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

  /** Shutdown the executor. */
  public void shutdown() {
    executor.shutdown();
    executor = null;
    counter.set(0);
  }
}
