package eflect.util;

import java.time.Duration;
import one.profiler.AsyncProfiler;
import one.profiler.Events;

/** Wrapper around the async-profiler that safely sets it up from an internal jar. */
public final class AsyncProfilerUtil {
  private static final int DEFAULT_RATE_MS = 1;
  // TODO(timurbey): this should probably come from a module
  private static final Duration asyncRate =
      Duration.ofMillis(
          Long.parseLong(
              System.getProperty("chappie.rate.async", Integer.toString(DEFAULT_RATE_MS))));
  private static final boolean noAsync = !setupAsync();

  /** Set up and start the async-profiler. */
  private static boolean setupAsync() {
    try {
      // only supporting sub-second for the moment
      AsyncProfiler.getInstance().start(Events.CPU, asyncRate.getNano());
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /** Returns un-split string of async-profiler records while safely pausing the profiler. */
  public static String readAsyncProfiler() {
    AsyncProfiler.getInstance().stop();
    String traces = AsyncProfiler.getInstance().dumpRecords();
    AsyncProfiler.getInstance().resume(Events.CPU, asyncRate.getNano());
    return traces;
  }
}
