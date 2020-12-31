package chappie.util;

import java.time.Instant;

/** Wrapper around the async-profiler that safely sets it up from an internal jar. */
public final class AsyncProfilerSample implements StackTraceSample {
  private final String stackTraces;

  public AsyncProfilerSample(String stackTraces) {
    this.stackTraces = stackTraces;
  }

  /** Return a dummy timestamp. */
  @Override
  public Instant getTimestamp() {
    return Instant.EPOCH;
  }

  /** Parse and return the jiffies from the stat strings. */
  @Override
  public String[] getStackTraces() {
    return stackTraces.split(System.lineSeparator());
  }
}
