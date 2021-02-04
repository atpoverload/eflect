package eflect.testing.data;

import eflect.data.Accountant;
import eflect.data.Sample;
import java.time.Instant;

/** Sample that only has a timestamp. */
public final class FakeSample<O> implements Sample {
  private final Instant timestamp;
  private final Accountant.Result result;
  private final O data;

  public FakeSample(Instant timestamp, Accountant.Result result, O data) {
    this.timestamp = timestamp;
    this.result = result;
    this.data = data;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  public Accountant.Result getResult() {
    return result;
  }

  public O getData() {
    return data;
  }
}
