package eflect;

import static eflect.util.LoggerUtil.getLogger;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.protos.sample.DataSet;
import eflect.sample.JiffiesDataSources;
import eflect.sample.RaplDataSources;
import eflect.sample.SampleCollector;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/** An unsafe interface to eflect. */
public final class Eflect {
  private static final Logger logger = getLogger();

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
  private static Context ctx;

  /** Creates an instance of the underlying class if it hasn't been created yet. */
  public static synchronized Eflect getInstance() {
    if (instance == null) {
      instance = new Eflect();
    }
    return instance;
  }

  /** Creates an instance of the underlying class if it hasn't been created yet. */
  public static synchronized Eflect getInstance(Context ctx) {
    if (instance == null) {
      instance = new Eflect(ctx);
      ctx = ctx;
    }
    return instance;
  }

  private final long periodMillis;

  private SampleCollector collector;

  private Eflect(Context ctx) {
    this.periodMillis =
        Long.parseLong(
            System.getProperty("eflect.period.default", Long.toString(DEFAULT_PERIOD_MS)));
    this.ctx = ctx;
  }

  /** Creates and starts a new collector. If there is no executor, a new thread pool is spun-up. */
  public void start(long periodMillis) {
    logger.info("starting eflect");
    // make sure we have an executor
    if (executor == null) {
      executor = newScheduledThreadPool(3, threadFactory);
    }
    // clear out the old collector
    if (collector != null) {
      collector.stop();
      collector.read();
      collector = null;
    }
    // make sure the period is valid
    Duration period = Duration.ofMillis(periodMillis);
    if (Duration.ZERO.equals(period)) {
      throw new RuntimeException("cannot sample with a period of " + period);
    }
    // TODO: abstract the collector; we want to be able to switch to an online version
    collector = new SampleCollector(executor);
    collector.start(JiffiesDataSources::sampleCpus, period);
    // collector.start(RaplDataSources::sampleRapl, period);
    collector.start(() -> BatteryManagerSources.sampleBatteryManager(ctx.getBatteryManager()), period);
    collector.start(JiffiesDataSources::sampleTasks, period);
  }

  /** Starts a collector with the default period. */
  public void start() {
    start(periodMillis);
  }

  /** Stops the collector. */
  public void stop() {
    collector.stop();
    logger.info("stopped eflect");
  }

  public DataSet read() {
    return collector.read();
  }

  /** Writes all sample data from the last session to the output directory. */
  public void dump(String outputPath) {
    logger.info("writing data set to " + outputPath);
    try (FileOutputStream output = new FileOutputStream(outputPath)) {
      collector.read().writeTo(output);
      logger.info("wrote data set to " + outputPath);
    } catch (Exception e) {
      logger.info("unable to write data");
      e.printStackTrace();
    }
  }

  /** Shutdown the executor. */
  public void shutdown() {
    executor.shutdown();
    executor = null;
  }
}
