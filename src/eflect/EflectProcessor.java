package eflect;

import clerk.Processor;
import eflect.data.Sample;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TreeMap;

/**
* Computes an estimate of application energy consumption from data across the
* runtime. This implements the same logic used in our data processing codebase
* (src/python/attribution) to compute runtime attribution. Data is stored in
* a typed collection and picked up by a processing method.
*/
public final class EflectProcessor implements Processor<Sample , Iterable<EnergyFootprint>> {
  private TreeMap<Instant, EflectSampleMerger> data = new TreeMap<>();

  /** Puts the data in relative timestamp-indexed storage to keep things sorted. */
  @Override
  public void add(Sample s) {
    synchronized(this) {
      Instant timestamp = Instant.now();
      data.putIfAbsent(timestamp, new EflectSampleMerger());
      data.get(timestamp).add(s);
    }
  }

  @Override
  public Iterable<EnergyFootprint> process() {
    int attempts = 0;
    ArrayList<EnergyFootprint> profiles = new ArrayList<>();
    EflectSampleMerger merger = new EflectSampleMerger();

    TreeMap<Instant, EflectSampleMerger> data;
    synchronized (this) {
      data = this.data;
      this.data = new TreeMap<>();
    }

    Instant lastTimestamp = Instant.now();
    for (Instant timestamp: data.keySet()) {
      merger = merger.merge(data.get(timestamp));
      if (merger.valid()) {
        profiles.add(merger.process());
        merger = new EflectSampleMerger();
      }
      lastTimestamp = timestamp;
    }

    synchronized(this) {
      merger = merger.merge(data.getOrDefault(lastTimestamp, new EflectSampleMerger()));
      data.put(lastTimestamp, merger);
    }

    return profiles;
  }
}
