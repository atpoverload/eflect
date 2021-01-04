package eflect.data.async;

import eflect.data.SampleCollection;
import eflect.data.StackTraceSample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/** Sample collection of async-profiler records. */
public final class AsyncProfilerSample implements SampleCollection<StackTraceSample> {
  private final String records;

  public AsyncProfilerSample(String records) {
    this.records = records;
  }

  /** Return a dummy timestamp. */
  @Override
  public Instant getTimestamp() {
    return Instant.EPOCH;
  }

  /** Parse and return the jiffies from the stat strings. */
  @Override
  public Collection<StackTraceSample> getSamples() {
    ArrayList<StackTraceSample> samples = new ArrayList();
    for (String record : records.split("\n")) {
      samples.add(new AsyncStackTraceSample(record));
    }
    return samples;
  }
}
