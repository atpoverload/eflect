package eflect.processing;

import static jrapl.Rapl.SOCKET_COUNT;
import static jrapl.Rapl.WRAP_AROUND_ENERGY;

import clerk.Processor;
import eflect.data.MachineSample;
import eflect.data.RaplSample;
import eflect.data.TaskSample;
import eflect.data.Sample;
import eflect.util.TimeUtil;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.Set;
import java.util.stream.Collectors;

/** Processor that merges samples into a footprint with task granularity. */
final class SampleMerger implements Processor<Sample, TaskEnergyFootprint> {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private static int cpuToSocket(int cpu) {
    return cpu * SOCKET_COUNT / CPU_COUNT;
  }

  private static long nonZeroMin(long first, long second){
    return first == 0 ? second : second == 0 ? first : Math.min(first, second);
  }

  private static double nonZeroMin(double first, double second){
    return first == 0 ? second : second == 0 ? first : Math.min(first, second);
  }

  private Instant start = Instant.MAX;
  private Instant end = Instant.MIN;

  // TODO(timurbey): this is a mess; how do we collapse this better?
  private final HashMap<Long, TaskRecord> taskMin = new HashMap<>();
  private final HashMap<Long, TaskRecord> taskMax = new HashMap<>();
  private final long[] appMin = new long[SOCKET_COUNT];
  private final long[] appMax = new long[SOCKET_COUNT];
  private final long[] cpuMin = new long[SOCKET_COUNT];
  private final long[] cpuMax = new long[SOCKET_COUNT];
  private final double[] energyMin = new double[SOCKET_COUNT];
  private final double[] energyMax = new double[SOCKET_COUNT];

  private boolean isActive = false;

  /** Puts the sample data into the correct container and adjust the values. */
  @Override
  public void add(Sample s) {
    if (s instanceof TaskSample) {
      for (String stat: s.getStats()) {
        TaskRecord task = new TaskRecord(stat);
        taskMin.put(task.tid, task);
        taskMax.put(task.tid, task);
      }
    } else if (s instanceof MachineSample) {
      for (String stat: s.getStats()) {
        MachineRecord machine = new MachineRecord(stat);
        cpuMin[machine.socket] += machine.jiffies;
        cpuMax[machine.socket] += machine.jiffies;
      }
    } else if (s instanceof RaplSample) {
      // i'd like this to be a String[] stats instead to avoid the cast
      double[] energy = ((RaplSample) s).getEnergy();
      for (int i = 0; i < SOCKET_COUNT; i++) {
        energyMin[i] += energy[i];
        energyMax[i] += energy[i];
      }
    } else {
      return; // break out so the timestamp isn't touched
    }
    // need to know the range of time for samples
    Instant timestamp = s.getTimestamp();
    start = TimeUtil.min(timestamp, start);
    end = TimeUtil.max(timestamp, end);
  }

  /**
   * Compute an {@link TaskEnergyFootprint} from the stored data.
   *
   * <p>There is no guarantee the result will be meaningful. Consult {@code valid()} and
   * {@code check()} to interpret the footprint.
   */
  @Override
  public TaskEnergyFootprint process() {
    HashMap<Long, Double> taskEnergy = new HashMap<>();

    double[] energy = new double[SOCKET_COUNT];
    long[] cpu = new long[SOCKET_COUNT];
    for (int socket = 0; socket < SOCKET_COUNT; socket++) {
      energy[socket] = energyMax[socket] - energyMin[socket];
      if (energy[socket] < 0) {
        energy[socket] += WRAP_AROUND_ENERGY;
      }

      cpu[socket] = cpuMax[socket] - cpuMin[socket];
    }

    for (long tid: taskMin.keySet()) {
      TaskRecord start = taskMin.get(tid);
      TaskRecord end = taskMax.get(tid);
      int socket = cpuToSocket(start.cpu);
      if (cpu[socket] > 0 && end.jiffies - start.jiffies > 0) {
        taskEnergy.put(tid, energy[socket] * Math.min(end.jiffies - start.jiffies, cpu[socket]) / cpu[socket]);
      }
    }

    return new TaskEnergyFootprint(start, end, taskEnergy);
  }

  /** Mutation-free merge using the min and max values as the ranges. */
  public SampleMerger merge(SampleMerger other) {
    SampleMerger merged = new SampleMerger();

    // TODO(timurbey): there really should be a better way to unite these
    Set<Long> tasks = Stream.concat(taskMin.keySet().stream(), other.taskMin.keySet().stream())
        .collect(Collectors.toSet());
    for (long tid: tasks) {
      TaskRecord minRecord;
      if (!other.taskMin.containsKey(tid)) {
        minRecord = taskMin.get(tid);
      } else if (!taskMin.containsKey(tid)) {
        minRecord = other.taskMin.get(tid);
      } else if (!taskMin.containsKey(tid)) {
        minRecord = other.taskMin.get(tid);
      } else if (taskMin.get(tid).jiffies < other.taskMin.get(tid).jiffies) {
        minRecord = taskMin.get(tid);
      } else {
        minRecord = other.taskMin.get(tid);
      }

      TaskRecord maxRecord;
      if (!other.taskMax.containsKey(tid) && !taskMax.containsKey(tid)) {
        maxRecord = minRecord;
      } else if (!other.taskMax.containsKey(tid)) {
        maxRecord = taskMax.get(tid);
      } else if (!taskMax.containsKey(tid)) {
        maxRecord = other.taskMax.get(tid);
      } else if (taskMax.get(tid).jiffies > other.taskMax.get(tid).jiffies) {
        maxRecord = taskMax.get(tid);
      } else {
        maxRecord = other.taskMax.get(tid);
      }

      merged.taskMin.put(tid, minRecord);
      merged.taskMax.put(tid, maxRecord);

      merged.appMin[cpuToSocket(minRecord.cpu)] += minRecord.jiffies;
      merged.appMax[cpuToSocket(minRecord.cpu)] += maxRecord.jiffies;
    }

    // TODO(timurbey): should be able to wrap these into a function
    for (int socket = 0; socket < SOCKET_COUNT; socket++) {
      merged.cpuMin[socket] = nonZeroMin(this.cpuMin[socket], other.cpuMin[socket]);
      merged.cpuMax[socket] = Math.max(this.cpuMax[socket], other.cpuMax[socket]);

      merged.energyMin[socket] = nonZeroMin(this.energyMin[socket], other.energyMin[socket]);
      merged.energyMax[socket] = Math.max(this.energyMax[socket], other.energyMax[socket]);
    }

    merged.start = TimeUtil.min(this.start, other.start);
    merged.end = TimeUtil.max(this.end, other.end);

    return merged;
  }

  /** Checks if the produced footprint is non-zero and not over-attributed. */
  boolean valid() {
    if (TimeUtil.equal(start, Instant.MAX) || TimeUtil.equal(end, Instant.MIN)) {
      return false;
    }
    for (int socket = 0; socket < SOCKET_COUNT; socket++) {
      double energy = energyMax[socket] - energyMin[socket];
      if (energy < 0) {
        energy += WRAP_AROUND_ENERGY;
      }
      long cpu = cpuMax[socket] - cpuMin[socket];
      long app = appMax[socket] - appMin[socket];
      if (energy == 0 || cpu == 0 || app == 0 || app > cpu) {
        return false;
      }
    }
    return true;
  }

  /** Checks if a footprint can be produced. */
  boolean check() {
    if (TimeUtil.equal(start, Instant.MAX) || TimeUtil.equal(end, Instant.MIN)) {
      return false;
    }
    return true;
  }

  private static class TaskRecord {
    private static final int STAT_LENGTH = 52;
    private static final int TID_INDEX = 0;
    private static final int CPU_INDEX = 38;
    private static final int[] JIFFY_INDICES = new int[] {13, 14};

    private final long tid;
    private final String name;
    private final int cpu;
    private final long jiffies;

    private TaskRecord(String stat) {
      String[] stats = stat.split(" ");
      int offset = stats.length - STAT_LENGTH;
      this.tid = Integer.parseInt(stats[TID_INDEX]);
      this.cpu = Integer.parseInt(stats[CPU_INDEX + offset]);
      String name = String.join(" ", Arrays.copyOfRange(stats, 1, 2 + offset));
      this.name = name.substring(1, name.length() - 1);
      long jiffies = 0;
      for (int i: JIFFY_INDICES) {
        jiffies += Long.parseLong(stats[i + offset]);
      }
      this.jiffies = jiffies;
    }
  }

  private static class MachineRecord {
    private static final int[] JIFFY_INDICES = new int[] {1, 2, 3, 6, 7, 8, 9, 10};

    private final int socket;
    private final long jiffies;

    private MachineRecord(String stat) {
      String[] stats = stat.split(" ");
      this.socket = cpuToSocket(Integer.parseInt(stats[0].substring(3)));
      long jiffies = 0;
      for (int i: JIFFY_INDICES) {
        jiffies += Long.parseLong(stats[i]);
      }
      this.jiffies = jiffies;
    }
  }
}
