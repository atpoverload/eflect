package eflect.data;

import clerk.Processor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/** Processor that merges accountants into groups of accounted data. */
public abstract class AccountantMerger<O> implements Processor<Sample, Collection<O>> {
  private final TreeMap<Instant, Accountant<Collection<O>>> data = new TreeMap<>();

  protected abstract Accountant<Collection<O>> newAccountant();

  /** Put the sample into a timestamped bucket. */
  @Override
  public final void add(Sample s) {
    synchronized (data) {
      if (!data.containsKey(s.getTimestamp())) {
        data.put(s.getTimestamp(), newAccountant());
      }
      data.get(s.getTimestamp()).add(s);
    }
  }

  /**
   * Accounts each timestamp, forward aggregating data that isn't {@link ACCOUNTED}. If the final
   * aggregate is accountable, it is also returned.
   */
  @Override
  public final Collection<O> process() {
    ArrayList<O> results = new ArrayList<>();
    Accountant<Collection<O>> accountant = null;
    synchronized (data) {
      for (Instant timestamp : data.keySet()) {
        if (accountant == null) {
          accountant = data.get(timestamp);
        } else {
          accountant.add(data.get(timestamp));
        }
        if (accountant.account() == Accountant.Result.ACCOUNTED) {
          results.addAll(accountant.process());
        }
      }
      data.clear();
      if (accountant != null && accountant.account() != Accountant.Result.UNACCOUNTABLE) {
        results.addAll(accountant.process());
      } else if (accountant != null) {
        data.put(Instant.EPOCH, accountant);
      }
    }
    return results;
  }
}
