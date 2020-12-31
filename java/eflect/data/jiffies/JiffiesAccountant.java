package eflect.data.jiffies;

import eflect.data.Accountant;
import eflect.data.Sample;
import eflect.data.ThreadActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.IntUnaryOperator;

/** Processor that merges /proc/ samples into {@link ThreadActivity}s. */
public final class JiffiesAccountant implements Accountant<Collection<ThreadActivity>> {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  private final int domainCount;
  private final IntUnaryOperator domainConversion;
  private final long[] statMin;
  private final long[] statMax;
  private final HashMap<Long, TaskStat> taskStatMin = new HashMap<>();
  private final HashMap<Long, TaskStat> taskStatMax = new HashMap<>();

  private ArrayList<ThreadActivity> data;

  public JiffiesAccountant(int domainCount, IntUnaryOperator domainConversion) {
    this.domainCount = domainCount;
    this.domainConversion = domainConversion;

    statMin = new long[CPU_COUNT];
    statMax = new long[CPU_COUNT];
    Arrays.fill(statMin, -1);
    Arrays.fill(statMax, -1);
  }

  /** Put the sample data into the correct containers. */
  @Override
  public void add(Sample sample) {
    if (sample instanceof ProcStatSample) {
      addProcStat(((ProcStatSample) sample).getJiffies());
    } else if (sample instanceof ProcTaskSample) {
      addProcTask(((ProcTaskSample) sample).getTaskStats());
    }
  }

  /** Add all samples from the other accountant to this if it is a {@link JiffiesAccountant}. */
  @Override
  public <T extends Accountant<Collection<ThreadActivity>>> void add(T o) {
    if (o instanceof JiffiesAccountant) {
      JiffiesAccountant other = (JiffiesAccountant) o;
      addProcStat(other.statMin);
      addProcStat(other.statMax);
      addProcTask(other.taskStatMin.values());
      addProcTask(other.taskStatMax.values());
    }
  }

  /**
   * Attempts to account the stored data.
   *
   * <p>Returns Result.UNACCOUNTABLE if a domain has no system or application jiffies.
   *
   * <p>Returns Result.OVERACCOUNTED if the tasks jiffies are greater than the system jiffies.
   *
   * <p>Returns Result.ACCOUNTED otherwise.
   */
  @Override
  public Accountant.Result account() {
    if (taskStatMin.isEmpty()) {
      return Accountant.Result.UNACCOUNTABLE;
    }

    // check the cpu jiffies
    long[] domainJiffies = new long[domainCount];
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      int domain = domainConversion.applyAsInt(cpu);
      domainJiffies[domain] += statMax[cpu] - statMin[cpu];
    }
    for (int domain = 0; domain < domainCount; domain++) {
      if (domainJiffies[domain] == 0) {
        return Accountant.Result.UNACCOUNTABLE;
      }
    }

    // check the task jiffies
    long[] applicationJiffies = new long[domainCount];
    ArrayList<ProcThreadActivityBuilder> tasks = new ArrayList<>();
    for (long id : taskStatMin.keySet()) {
      TaskStat task = taskStatMin.get(id);
      long jiffies = taskStatMax.get(id).jiffies - task.jiffies;
      int domain = domainConversion.applyAsInt(task.cpu);
      applicationJiffies[domain] += jiffies;
      tasks.add(
          new ProcThreadActivityBuilder()
              .setId(task.id)
              .setName(task.name)
              .setDomain(domain)
              .setTaskJiffies(jiffies));
    }

    for (int domain = 0; domain < domainCount; domain++) {
      if (applicationJiffies[domain] == 0) {
        return Accountant.Result.UNACCOUNTABLE;
      }
    }

    // if we got here, we can produce **something**
    for (int domain = 0; domain < domainCount; domain++) {
      // check if the application jiffies exceeds the system jiffies
      if (applicationJiffies[domain] > domainJiffies[domain]) {
        data = accountTasks(applicationJiffies, tasks);
        return Accountant.Result.OVERACCOUNTED;
      }
    }
    data = accountTasks(domainJiffies, tasks);
    return Accountant.Result.ACCOUNTED;
  }

  /** Returns the data if it's accountable. Otherwise, null is returned. */
  // TODO(timurbey): i don't like discarding implicitly; maybe the accountant needs `discard()`.
  @Override
  public Collection<ThreadActivity> process() {
    if (data != null || account() == Accountant.Result.ACCOUNTED) {
      for (int domain = 0; domain < domainCount; domain++) {
        statMin[domain] = statMax[domain];
      }
      taskStatMin.clear();
      taskStatMin.putAll(taskStatMax);
    }
    return data;
  }

  private synchronized void addProcStat(long[] jiffies) {
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      if (statMin[cpu] < 0 || jiffies[cpu] < statMin[cpu]) {
        statMin[cpu] = jiffies[cpu];
      }
      if (jiffies[cpu] > statMax[cpu]) {
        statMax[cpu] = jiffies[cpu];
      }
    }
  }

  private synchronized void addProcTask(Iterable<TaskStat> stats) {
    for (TaskStat stat : stats) {
      if (!taskStatMin.containsKey(stat.id) || stat.jiffies < taskStatMin.get(stat.id).jiffies) {
        taskStatMin.put(stat.id, stat);
      }
      if (!taskStatMax.containsKey(stat.id) || stat.jiffies > taskStatMax.get(stat.id).jiffies) {
        taskStatMax.put(stat.id, stat);
      }
    }
  }

  private ArrayList<ThreadActivity> accountTasks(
      long[] jiffies, ArrayList<ProcThreadActivityBuilder> tasks) {
    ArrayList<ThreadActivity> activity = new ArrayList<>();
    for (ProcThreadActivityBuilder task : tasks) {
      task.setTotalJiffies(jiffies[task.domain]);
      if (task.getActivity() > 0) {
        activity.add(task.build());
      }
    }
    return activity;
  }
}
