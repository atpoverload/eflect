package eflect.testing.data;

import eflect.data.Accountant;
import eflect.data.Sample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/** Processor that merges samples into a footprint with task granularity. */
public final class FakeAccountant<O> implements Accountant<Collection<O>> {
  private Instant timestamp = Instant.MIN;
  private Accountant.Result result = Accountant.Result.UNACCOUNTABLE;
  private Collection<O> data = new ArrayList<>();

  public FakeAccountant() {}

  /** Puts the data from {@link FakeSample}s into the fields. */
  @Override
  public void add(Sample s) {
    if (s instanceof FakeSample) {
      FakeSample<Collection<O>> sample = (FakeSample<Collection<O>>) s;
      timestamp = sample.getTimestamp();
      data = sample.getData();
      result = sample.getResult();
    }
  }

  /** Adds the data if it's a FakeAccountant. */
  // TODO(timurbey): this doesn't really make sense for setting the result atm
  @Override
  public <T extends Accountant<Collection<O>>> void add(T other) {
    if (other instanceof FakeAccountant) {
      FakeAccountant accountant = (FakeAccountant) other;
      Collection<O> data = new ArrayList<>();
      this.timestamp = this.timestamp != Instant.MIN ? this.timestamp : accountant.timestamp;
      data.addAll(this.data);
      data.addAll(accountant.data);
      this.data = data;
      this.result = this.result != Result.UNACCOUNTABLE ? this.result : accountant.result;
    }
  }

  /** Returns the stored result. */
  @Override
  public Accountant.Result account() {
    return result;
  }

  /** Returns the stored data. */
  @Override
  public Collection<O> process() {
    if (result == Accountant.Result.UNACCOUNTABLE) {
      return null;
    }
    return data;
  }

  /** Does nothing with the data. */
  @Override
  public void discardStart() {
    timestamp = Instant.MIN;
    result = Accountant.Result.UNACCOUNTABLE;
    data = new ArrayList<>();
  }

  /** Does nothing with the data. */
  @Override
  public void discardEnd() {
    timestamp = Instant.MIN;
    result = Accountant.Result.UNACCOUNTABLE;
    data = new ArrayList<>();
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public void setResult(Accountant.Result result) {
    this.result = result;
  }

  public void setData(Collection<O> data) {
    this.data = data;
  }
}
