package eflect.experiments;

import static eflect.util.LoggerUtil.getLogger;
import static eflect.util.WriterUtil.writeCsv;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.CpuFreqMonitor;
import eflect.LinuxEflect;
import eflect.data.EnergyFootprint;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import jrapl.Rapl;

/** A wrapper around {@link LinuxEflect} that also monitors runtime stats and calmness. */
public final class EflectCalmnessMonitor {
  private static final Logger logger = getLogger();
  private static final AtomicInteger counter = new AtomicInteger();
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String FOOTPRINT_HEADER =
      "id,name,start,end,domain,app_energy,total_energy,trace";

  private static EflectCalmnessMonitor instance;

  /** Creates an instance of the underlying class if it hasn't been created yet. */
  public static synchronized EflectCalmnessMonitor getInstance() {
    if (instance == null) {
      instance = new EflectCalmnessMonitor();
    }
    return instance;
  }

  private final String outputPath;
  private final long periodMillis;
  private final Instant[] time = new Instant[2];
  private final double[][][] energy = new double[2][][];

  private ScheduledExecutorService executor;
  private LinuxEflect eflect;
  private CpuFreqMonitor freqMonitor;
  private Collection<EnergyFootprint> footprints;
  private Collection<EnergyFootprint> frequencies;

  // TODO(timur): i'm not sure how much this class wrapper needs to do
  private EflectCalmnessMonitor() {
    this.outputPath = System.getProperty("eflect.output", ".");
    this.periodMillis = Long.parseLong(System.getProperty("eflect.period", "64"));
  }

  /**
   * Creates and starts instances of eflect and the calmness monitor.
   *
   * <p>If there is no existing executor, a new thread pool is spun-up.
   *
   * <p>If the period is 0, an eflect will not be created.
   */
  public void start() {
    start(periodMillis);
  }

  /** Starts up eflect if needed. */
  public void start(long periodMillis) {
    logger.info("starting eflect");
    if (executor == null) {
      executor = newScheduledThreadPool(5, threadFactory);
    }
    Duration period = Duration.ofMillis(periodMillis);
    if (!Duration.ZERO.equals(period)) {
      eflect = new LinuxEflect(executor, period);
      footprints = null;
    } else {
      eflect = null;
    }
    freqMonitor = new CpuFreqMonitor(executor, Duration.ofMillis(500));

    if (!Duration.ZERO.equals(period)) {
      eflect.start();
    }
    freqMonitor.start();
    time[0] = Instant.now();
    energy[0] = Rapl.getInstance().getEnergyStats();
  }

  /** Stops any running collectors. */
  public void stop() {
    time[1] = Instant.now();
    energy[1] = Rapl.getInstance().getEnergyStats();
    if (eflect != null) {
      eflect.stop();
    }
    freqMonitor.stop();

    logger.info("stopped eflect");
    logger.info("ran for " + Duration.between(time[0], time[1]).toString());

    double consumed = 0;
    for (int domain = 0; domain < Rapl.getInstance().getSocketCount(); domain++) {
      for (int component = 0; component < 3; component++) {
        double componentEnergy = energy[1][domain][component] - energy[0][domain][component];
        consumed += componentEnergy;
      }
    }
    if (consumed < 0) {
      consumed += Rapl.getInstance().getWrapAroundEnergy();
    }
    logger.info("system consumed " + consumed + "J");

    if (eflect != null) {
      footprints = eflect.read();
      eflect = null;
      logger.info("runtime consumed " + Double.toString(sum(footprints)) + "J");
    } else {
      footprints = null;
    }
  }

  // TODO(timur): all of these dump methods need to be updated when we change the footprint.
  public void dump() {
    File dataDirectory = getOutputDirectory();
    if (footprints != null) {
      writeCsv(dataDirectory.getPath(), "footprint.csv", FOOTPRINT_HEADER, footprints);
    }
    writeFreqs("calmness.csv");
  }

  public void dump(String tag) {
    File dataDirectory = getOutputDirectory();
    if (footprints != null) {
      writeCsv(dataDirectory.getPath(), "footprint-" + tag + ".csv", FOOTPRINT_HEADER, footprints);
    }
    writeFreqs("calmness-" + tag + "-.csv");
  }

  /** Shutdown the executor. */
  public void shutdown() {
    executor.shutdown();
    executor = null;
  }

  private File getOutputDirectory() {
    File outputDir = new File(outputPath);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    return outputDir;
  }

  private void writeFreqs(String fileName) {
    String[] cpus = new String[CPU_COUNT];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      cpus[cpu] = Integer.toString(cpu);
    }
    writeCsv(
        getOutputDirectory().getPath(),
        fileName,
        String.join(",", "timestamp", String.join(",", cpus)), // header
        List.of(freqMonitor.read())); // data
  }

  private static double sum(Iterable<EnergyFootprint> footprints) {
    double energy = 0;
    for (EnergyFootprint footprint : footprints) {
      energy += footprint.energy;
    }
    return energy;
  }
}
