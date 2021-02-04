package eflect.data.async;

import eflect.data.Sample;
import eflect.data.SampleCollection;
import eflect.data.StackTraceSample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/** Sample collection of async-profiler records. */
public final class AsyncProfilerSample implements SampleCollection {
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
  public Collection<Sample> getSamples() {
    ArrayList<Sample> samples = new ArrayList();
    for (String record : records.split("\n")) {
      String[] entries = record.split(",");
      Instant timestamp = Instant.ofEpochMilli(Long.parseLong(entries[0]));
      long id = Long.parseLong(entries[1]);
      String stackTrace = entries[2];
      samples.add(new StackTraceSample(timestamp, id, stackTrace));
    }
    return samples;
  }
}
