package eflect.data.async;

import java.time.Duration;
import java.time.Instant;
import one.profiler.AsyncProfiler;
import one.profiler.Events;

/** A Clerk that estimates the energy consumed by an application on an intel linux system. */
public final class AsyncProfilerDataSources {
  private static final Duration ASYNC_PERIOD =
      Duration.ofMillis(Integer.parseInt(System.getProperty("eflect.async.period", "2")));
  private static final Duration ASYNC_COLLECTION_PERIOD =
      Duration.ofMillis(
          Integer.parseInt(System.getProperty("eflect.async.collection.period", "500")));

  private static boolean asyncRunning = false;
  private static Instant last = Instant.now();

  public static synchronized AsyncProfilerSample sampleAsyncProfiler() {
    Instant now = Instant.now();
    if (Duration.between(last, now).toMillis() > ASYNC_COLLECTION_PERIOD.toMillis()) {
      if (!asyncRunning) {
        AsyncProfiler.getInstance().start(Events.CPU, ASYNC_PERIOD.getNano());
        asyncRunning = true;
      }
      AsyncProfiler.getInstance().stop();
      String traces = AsyncProfiler.getInstance().dumpRecords();
      AsyncProfiler.getInstance().resume(Events.CPU, ASYNC_PERIOD.getNano());
      last = now;
      return new AsyncProfilerSample(now, traces);
    }
    return new AsyncProfilerSample(now, "");
  }
}
