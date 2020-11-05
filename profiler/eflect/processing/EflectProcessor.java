package eflect.processing;

import clerk.Processor;
import eflect.EnergyFootprint;
import eflect.data.Sample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/** A processor that collapses samples into {@link TaskEnergyFootprint}s. */
public final class EflectProcessor implements Processor<Sample, List<EnergyFootprint<?>>> {
  private TreeMap<Instant, SampleMerger> data = new TreeMap<>();

  /** Places the sample in a sorted, timestamp-indexed bucket. */
  @Override
  public void add(Sample s) {
    synchronized (data) {
      Instant timestamp = Instant.now();
      data.putIfAbsent(timestamp, new SampleMerger());
      data.get(timestamp).add(s);
    }
  }

  /**
   * Grabs the stored data and forward scans the data for valid footprints. If a timestamp cannot
   * produce a valid footprint, the data is merged with the next timestamp until a valid footprint
   * is produced. Once all data is consumed, the invalid merged data is replaced into storage.
   */
  @Override
  public List<EnergyFootprint<?>> process() {
    ArrayList<EnergyFootprint<?>> profiles = new ArrayList<>();
    SampleMerger merger = new SampleMerger();

    // this is a very fast lock because it just creates a new object; is this a problem?
    TreeMap<Instant, SampleMerger> data = this.data;
    synchronized (this.data) {
      this.data = new TreeMap<>();
    }

    // aggregate the samples until we run out
    Instant lastTimestamp = Instant.now();
    for (Instant timestamp : data.keySet()) {
      merger = merger.merge(data.get(timestamp));
      if (merger.valid()) {
        profiles.add(merger.process());
        merger = new SampleMerger();
      }
      lastTimestamp = timestamp;
    }

    // consume the last one if possible; otherwise, replace it
    if (merger.check()) {
      profiles.add(merger.process());
    } else {
      synchronized (this.data) {
        merger = merger.merge(this.data.getOrDefault(lastTimestamp, new SampleMerger()));
        this.data.put(lastTimestamp, merger);
      }
    }

    return profiles;
  }
}
