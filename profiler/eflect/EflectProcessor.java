package eflect;

import clerk.Processor;
import eflect.data.Sample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
/**
 * A processor that stores samples in timestamp-indexed storage and can
 * collapse samples into {@link EnergyFootprint}s.
 */
public final class EflectProcessor implements Processor<Sample, List<EnergyFootprint>> {
  private TreeMap<Instant, EflectSampleMerger> data = new TreeMap<>();

  /** Places the sample in a sorted, timestamp-indexed bucket. */
  @Override
  public void accept(Sample s) {
    synchronized(data) {
      Instant timestamp = Instant.now();
      data.putIfAbsent(timestamp, new EflectSampleMerger());
      data.get(timestamp).accept(s);
    }
  }

  /**
   * Grabs the stored data and forward scans the data for valid footprints. If
   * a timestamp cannot produce a valid footprint, the data is merged with the
   * next timestamp until a valid footprint is produced. Once all data is
   * consumed, the invalid merged data is replaced into storage.
   */
  @Override
  public List<EnergyFootprint> get() {
    ArrayList<EnergyFootprint> profiles = new ArrayList<>();
    EflectSampleMerger merger = new EflectSampleMerger();

    // this is a very fast lock but it just creates a new object; is this a problem?
    TreeMap<Instant, EflectSampleMerger> data = this.data;
    synchronized (this.data) {
      this.data = new TreeMap<>();
    }

    Instant lastTimestamp = Instant.now();
    for (Instant timestamp: data.keySet()) {
      merger = merger.merge(data.get(timestamp));
      if (merger.valid()) {
        profiles.add(merger.get());
        merger = new EflectSampleMerger();
      }
      lastTimestamp = timestamp;
    }

    if (merger.check()) {
      profiles.add(merger.get());
    } else {
      synchronized(this.data) {
        merger = merger.merge(this.data.getOrDefault(lastTimestamp, new EflectSampleMerger()));
        this.data.put(lastTimestamp, merger);
      }
    }

    return profiles;
  }
}
