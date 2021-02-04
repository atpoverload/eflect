package eflect.data.async;

import eflect.data.Sample;
import eflect.data.SampleCollection;
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
      String[] entries = data.split(",");
      Instant timestamp = Instant.ofEpochMilli(Long.parseLong(data.split(",")[0]));
      int id = id.split(",")[1];
      String stackTrace = data.split(",")[2];
      samples.add(new StackTraceSample(timestamp, id, stackTrace));
    }
    return samples;
  }
}
