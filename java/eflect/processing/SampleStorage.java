package eflect.data;

import static java.util.Collections.unmodifiableMap;

import clerk.Processor;
import java.util.ArrayList;
import java.util.HashMap;

/** A processor that writes csv files from samples by sample type. */
public final class SampleStorage implements Processor<Sample, Map<Class<?>, List<Sample>>> {
  private final HashMap<Class<?>, List<Sample>> data = new HashMap<>();
  protected final String outputPath;

  public SampleStorage() {}

  @Override
  public final void add(Sample sample) {
    synchronized (data) {
      if (!data.containsKey(sample.getClass())) {
        data.put(sample.getClass(), new ArrayList<>());
      }
      data.get(sample.getClass()).add(sample);
    }
  }

  @Override
  public final Map<Class<?>, List<Sample>> process() {
    return unmodifiableMap(data);
  }
}
