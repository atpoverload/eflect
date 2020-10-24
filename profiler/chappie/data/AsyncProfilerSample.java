package chappie.data;

import static chappie.util.AsyncProfilerUtil.sampleStackTraces;

import eflect.data.Sample;

/** Snapshot of the application's jiffies by task. */
public class AsyncProfilerSample implements Sample {
  private final String traces;

  public AsyncProfilerSample() {
    this.traces = sampleStackTraces();
  }

  @Override
  public String[] getStats() {
    return traces.split("\n");
  }
}
