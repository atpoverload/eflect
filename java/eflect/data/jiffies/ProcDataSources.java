package eflect.data.jiffies;

import eflect.data.Sample;
import eflect.util.LoggerUtil;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

/** A clerk that collects jiffies and energy data for an intel linux system. */
public final class ProcDataSources {
  private static final Logger logger = LoggerUtil.getLogger();

  private static final long PID = ProcessHandle.current().pid();
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final String SYSTEM_STAT_FILE = String.join(File.separator, "/proc", "stat");
  private static final String APPLICATION_TASK_DIR =
      String.join(File.separator, "/proc", Long.toString(PID), "task");

  /** Reads the system's stat file and returns a timestamped sample. */
  public static ProcStatSample sampleProcStat() {
    String[] stats = new String[CPU_COUNT];
    try (BufferedReader reader = Files.newBufferedReader(Path.of(SYSTEM_STAT_FILE))) {
      reader.readLine(); // first line is total summary; we need by cpu
      for (int i = 0; i < CPU_COUNT; i++) {
        stats[i] = reader.readLine();
      }
    } catch (Exception e) {
      logger.info("unable to read " + SYSTEM_STAT_FILE);
      e.printStackTrace();
    } finally {
      return new ProcStatSample(Instant.now(), stats);
    }
  }

  /** Reads this application's thread's stat files and returns a timestamped sample. */
  public static ProcTaskSample sampleTaskStats() {
    ArrayList<String> stats = new ArrayList<String>();
    File tasks = new File(APPLICATION_TASK_DIR);
    for (File task : tasks.listFiles()) {
      File statFile = new File(task, "stat");
      if (!statFile.exists()) {
        continue;
      }
      try {
        stats.add(Files.readString(Path.of(statFile.getPath())));
      } catch (Exception e) {
        logger.info("unable to read task " + statFile);
      }
    }
    return new ProcTaskSample(Instant.now(), stats);
  }

  public static List<Supplier<Sample>> getProcSources() {
    Supplier<Sample> stat = ProcDataSources::sampleProcStat;
    Supplier<Sample> task = ProcDataSources::sampleTaskStats;
    return List.of(stat, task);
  }

  private ProcDataSources() {}
}
