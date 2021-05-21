package eflect.experiments;

import static eflect.util.LoggerUtil.getLogger;
import static eflect.util.WriterUtil.writeCsv;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import clerk.Clerk;
import eflect.EflectSampleCollector;
import eflect.data.Sample;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/** A wrapper around {@link EflectSampleCollector}. */
public final class EflectMonitor {
  private static final Logger logger = getLogger();
  private static final AtomicInteger counter = new AtomicInteger();
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };

  private static ScheduledExecutorService executor;
  private static EflectMonitor instance;

  /** Creates an instance of the underlying class if it hasn't been created yet. */
  public static synchronized EflectMonitor getInstance() {
    if (instance == null) {
      instance = new EflectMonitor();
    }
    return instance;
  }

  private final String outputPath;
  private final long periodMillis;

  private int sessionCount = 0;
  private Clerk<?> clerk;

  private EflectMonitor() {
    this.outputPath = System.getProperty("eflect.output", ".");
    this.periodMillis = Long.parseLong(System.getProperty("eflect.period", "50"));
  }

  /** Creates and starts a new collector. If there is no executor, a new thread pool is spun-up. */
  public void start(long periodMillis) {
    logger.info("starting eflect");
    if (executor == null) {
      executor = newScheduledThreadPool(4, threadFactory);
    }
    Duration period = Duration.ofMillis(periodMillis);
    if (Duration.ZERO.equals(period)) {
      throw new RuntimeException("cannot sample with a period of " + period);
    }
    // TODO: abstract the collector; we want to be able to switch to an online version
    clerk = new EflectSampleCollector(executor, period);
    clerk.start();
    sessionCount++;
  }

  /** Starts a collector with the default period. */
  public void start() {
    start(periodMillis);
  }

  /** Stops the collector. */
  public void stop() {
    clerk.stop();
    logger.info("stopped eflect");
  }

  /** Writes all sample data from the last session to the output directory. */
  public void dump() {
    logger.info("writing data for session " + Integer.toString(sessionCount));

    // TODO: abstract the output; we need to read for online
    File outputDir = new File(outputPath, Integer.toString(sessionCount));
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }

    // TODO: abstract this for the different types; we need to handle a footprint
    Map<Class<?>, List<Sample>> data = (Map<Class<?>, List<Sample>>) clerk.read();
    for (Class<?> cls : data.keySet()) {
      String[] clsName = cls.toString().split("\\.");
      writeCsv(outputDir.getPath(), clsName[clsName.length - 1] + ".csv", data.get(cls));
    }

    logger.info("wrote data to " + outputDir.toString());
  }

  /** Shutdown the executor. */
  public void shutdown() {
    executor.shutdown();
    executor = null;
  }
}
