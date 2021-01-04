package eflect.data.async;

import eflect.data.StackTraceSample;
import java.time.Instant;

/** Sample of a single async-profiler record. */
public final class AsyncStackTraceSample implements StackTraceSample {
  private final String data;

  public AsyncStackTraceSample(String data) {
    this.data = data;
  }

  /** Return a dummy timestamp. */
  @Override
  public Instant getTimestamp() {
    return Instant.ofEpochMilli(Long.parseLong(data.split(",")[1]));
  }

  /** Parse and return the jiffies from the stat strings. */
  @Override
  public long getId() {
    return Long.parseLong(data.split(",")[0]);
  }

  /** Parse and return the jiffies from the stat strings. */
  @Override
  public String getStackTrace() {
    return data.split(",")[2];
  }
}
