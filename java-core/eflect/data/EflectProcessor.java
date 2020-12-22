package eflect.data;

import clerk.Processor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/** Processor that merges accountants into a footprint with task granularity. */
public abstract class EflectProcessor implements Processor<Sample, Collection<EnergyFootprint>> {
  private final TreeMap<Instant, EnergyAccountant> data = new TreeMap<>();

  protected abstract EnergyAccountant newAccountant();

  /** Put the sample into a timestamped bucket. */
  @Override
  public final void add(Sample s) {
    synchronized (data) {
      if (!data.containsKey(s.getTimestamp())) {
        data.put(s.getTimestamp(), newAccountant());
      }
    }
    data.get(s.getTimestamp()).add(s);
  }

  /** Accounts each timestamp, forward aggregating data that isn't {@link ACCOUNTED}. */
  @Override
  public final Collection<EnergyFootprint> process() {
    ArrayList<EnergyFootprint> footprints = new ArrayList<>();
    EnergyAccountant accountant = null;
    for (Instant timestamp : data.keySet()) {
      if (accountant == null) {
        accountant = data.get(timestamp);
      } else {
        accountant.add(data.get(timestamp));
      }

      if (accountant.isAccountable() == Accountant.Result.ACCOUNTED) {
        footprints.addAll(accountant.process());
      }
    }
    data.clear();
    if (accountant != null && accountant.isAccountable() != Accountant.Result.UNACCOUNTABLE) {
      footprints.addAll(accountant.process());
    } else if (accountant != null) {
      data.put(Instant.EPOCH, accountant);
    }
    return footprints;
  }
}
