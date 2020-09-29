package eflect;

import clerk.Processor;
import eflect.data.Sample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TreeMap;
/**
 * A processor that stores samples in timestamp-indexed storage and can
 * collapse samples into {@link EnergyFootprint}s.
 */
public final class EflectProcessor implements Processor<Sample, Iterable<EnergyFootprint>> {
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
  public Iterable<EnergyFootprint> get() {
    ArrayList<EnergyFootprint> profiles = new ArrayList<>();
    int attempts = 0;
    synchronized(this) {
      EflectSampleMerger merger = new EflectSampleMerger();

      TreeMap<Instant, EflectSampleMerger> data;
      synchronized (this) {
        data = this.data;
        this.data = new TreeMap<>();
      }

      Instant lastTimestamp = Instant.now();
      for (Instant timestamp: data.keySet()) {
        merger = merger.merge(data.get(timestamp));
        if (merger.valid() || (attempts++ > 25 && merger.check())) {
          profiles.add(merger.get());
          merger = new EflectSampleMerger();
        }
        lastTimestamp = timestamp;
      }

      synchronized(data) {
        merger = merger.merge(data.getOrDefault(lastTimestamp, new EflectSampleMerger()));
        data.put(lastTimestamp, merger);
      }
    }

    return profiles;
  }
}
