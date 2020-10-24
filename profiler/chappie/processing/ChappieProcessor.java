package chappie.processing;

import chappie.data.AsyncProfilerSample;
import clerk.Processor;
import eflect.EnergyFootprint;
import eflect.processing.EflectProcessor;
import eflect.processing.TaskEnergyFootprint;
import eflect.data.Sample;
import eflect.util.TimeUtil;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;

/** A processor that aligns methods to {@link TaskEnergyFootprint}s. */
public final class ChappieProcessor implements Processor<Sample, List<MethodEnergyFootprint>> {
  private final EflectProcessor eflect = new EflectProcessor();

  private TreeMap<Instant, ArrayList<TraceRecord>> traces = new TreeMap<>();

  /** Puts the data in relative timestamp-indexed storage to keep things sorted. */
  @Override
  public void add(Sample s) {
    if (s instanceof AsyncProfilerSample) {
      for (String stat: s.getStats()) {
        String[] stats = stat.split(",");
        Instant timestamp = Instant.ofEpochMilli(Long.parseLong(stats[0]));
        synchronized (this.traces) {
          this.traces.putIfAbsent(timestamp, new ArrayList<TraceRecord>());
          this.traces.get(timestamp).add(new TraceRecord(Long.parseLong(stats[1]), stats[2]));
        }
      }
    } else if (s instanceof Sample) {
      eflect.add(s);
    }
  }

  @Override
  public List<MethodEnergyFootprint> process() {
    // setup storage
    ArrayList<MethodEnergyFootprint> footprints = new ArrayList<>();

    // acquire data
    TreeMap<Instant, ArrayList<TraceRecord>> traces = this.traces;
    synchronized(this.traces) {
      this.traces = new TreeMap<>();
    }

    Iterator<Instant> timestamps = traces.keySet().iterator();

    // prime loop
    Instant timestamp = Instant.EPOCH;
    // TODO(timurbey): there should be some sort of guarantee of sorted data and typing
    for (TaskEnergyFootprint footprint: eflect.process()) {
      // setup storage
      HashMap<Long, ArrayList<String>> methodGroups = new HashMap<Long, ArrayList<String>>();
      for (;;) {
        // move to a function?
        if (TimeUtil.equal(timestamp, Instant.EPOCH)) {
          if (timestamps.hasNext()) {
            timestamp = timestamps.next();
          } else {
            break;
          }
        }

        if (TimeUtil.greaterThan(timestamp, footprint.getEnd())) {
          break;
        } else if (TimeUtil.atLeast(timestamp, footprint.getStart())) {
          for (TraceRecord trace: traces.get(timestamp)) {
            methodGroups.putIfAbsent(trace.tid, new ArrayList<>());
            methodGroups.get(trace.tid).add(trace.trace);
          }
          timestamp = Instant.EPOCH;
        } else {
          timestamp = Instant.EPOCH;
        }
      }

      HashMap<String, Double> methodRanking = new HashMap<>();
      for (long tid: methodGroups.keySet()) {
        try {
          for (String trace: methodGroups.get(tid)) {
            methodRanking.put(trace, methodRanking.getOrDefault(trace, 0.0) + footprint.getEnergy(tid) / methodGroups.get(tid).size());
          }
        } catch (Exception e) {
          // this breaks if the tid is missing
        }
      }
      footprints.add(new MethodEnergyFootprint(footprint.getStart(), footprint.getEnd(), methodRanking));
    }
    return footprints;
  }

  private static class TraceRecord {
    private final long tid;
    private final String trace;

    private TraceRecord(long tid, String trace) {
      this.tid = tid;
      this.trace = trace;
    }
  }
}
