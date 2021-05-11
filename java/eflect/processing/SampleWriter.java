package eflect.data;

import static eflect.util.WriterUtil.writeCsv;

import clerk.Processor;
import java.util.ArrayList;
import java.util.HashMap;

/** A processor that writes csv files from samples by sample type. */
public final class SampleWriter implements Processor<Sample, Boolean> {
  private final HashMap<Class<?>, ArrayList<Sample>> data = new HashMap<>();
  protected final String outputPath;

  public SampleWriter(String outputPath) {
    this.outputPath = outputPath;
  }

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
  public final Boolean process() {
    for (Class<?> cls : data.keySet()) {
      String[] clsName = cls.toString().split("\\.");
      writeCsv(outputPath, clsName[clsName.length - 1] + ".csv", data.get(cls));
    }
    return true;
  }
}
