package eflect.data;

import clerk.Processor;
import eflect.util.RangeMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

/** Processor that aligns stack traces to energy footprints. */
public final class StackTraceAligner implements Processor<Sample, Collection<EnergyFootprint>> {
  private final TreeMap<Instant, Collection<StackTraceSample>> samples = new TreeMap<>();
  private final Processor<Sample, Collection<EnergyFootprint>> energyAccountant;

  public StackTraceAligner(Processor<Sample, Collection<EnergyFootprint>> energyAccountant) {
    this.energyAccountant = energyAccountant;
  }

  /** Put the sample data into the correct container. */
  @Override
  public void add(Sample s) {
    if (s instanceof SampleCollection) {
      Collection<Sample> samples = ((Collection<Sample>) ((SampleCollection) s).getSamples());
      for (Sample sample : samples) {
        add(sample);
      }
    } else if (s instanceof StackTraceSample) {
      addSample((StackTraceSample) s);
    } else {
      energyAccountant.add(s);
    }
  }

  /**
   * Returns the energy footprints of the underlying accountant and aligns stack traces collected in
   * corresponding time ranges.
   */
  @Override
  public Collection<EnergyFootprint> process() {
    RangeMap<Instant, HashMap<Long, EnergyFootprint.Builder>> footprintGroups = getFootprints();
    synchronized (samples) {
      for (Instant timestamp : samples.keySet()) {
        if (footprintGroups.contains(timestamp)) {
          for (StackTraceSample sample : samples.get(timestamp)) {
            if (footprintGroups.get(timestamp).containsKey(sample.getId())) {
              footprintGroups
                  .get(timestamp)
                  .get(sample.getId())
                  .addStackTrace(sample.getStackTrace());
            }
          }
        }
      }
      samples.clear();
    }
    ArrayList<EnergyFootprint> footprints = new ArrayList<>();
    for (HashMap<?, EnergyFootprint.Builder> group : footprintGroups.values()) {
      for (EnergyFootprint.Builder footprint : group.values()) {
        footprints.add(footprint.build());
      }
    }
    return footprints;
  }

  private void addSample(StackTraceSample sample) {
    synchronized (samples) {
      if (!samples.containsKey(sample.getTimestamp())) {
        samples.put(sample.getTimestamp(), new ArrayList<StackTraceSample>());
      }
      samples.get(sample.getTimestamp()).add(sample);
    }
  }

  private RangeMap<Instant, HashMap<Long, EnergyFootprint.Builder>> getFootprints() {
    HashMap<Instant, HashMap<Long, EnergyFootprint.Builder>> footprints = new HashMap<>();
    for (EnergyFootprint footprint : energyAccountant.process()) {
      if (!footprints.containsKey(footprint.start)) {
        footprints.put(footprint.start, new HashMap<Long, EnergyFootprint.Builder>());
      }
      footprints.get(footprint.start).put(footprint.id, footprint.toBuilder());
      if (!footprints.containsKey(footprint.end)) {
        footprints.put(footprint.end, new HashMap<Long, EnergyFootprint.Builder>());
      }
    }
    return new RangeMap<>(footprints);
  }
}
