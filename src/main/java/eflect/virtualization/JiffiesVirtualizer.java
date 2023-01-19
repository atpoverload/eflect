package eflect.virtualization;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.protobuf.util.Timestamps;
import eflect.EflectDataSet;
import eflect.sample.CpuReading;
import eflect.sample.CpuSample;
import eflect.sample.TaskReading;
import eflect.sample.TaskSample;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JiffiesVirtualizer {
  /** Tries to align the {@link TaskSamples} and {@link CpuSamples} into jiffy virtualizations. */
  public static List<Difference<Activity>> virtualize(EflectDataSet data, long millisThresh) {   
    if (data.getTaskCount() < 2 || data.getCpuCount() < 2) {
      System.out.println("not enough samples to align data");
      return new ArrayList<>();
    }

    Iterator<Difference<TaskReading>> tasks = processTasks(data.getTaskList()).iterator();
    Iterator<Difference<CpuReading>> cpus = processCpus(data.getCpuList()).iterator();

    Difference<TaskReading> task = tasks.next();
    Difference<CpuReading> cpu = cpus.next();

    ArrayList<Difference<Activity>> jiffies = new ArrayList<>();
    while (true) {
      long taskStart = Timestamps.toMillis(task.start) / millisThresh;
      long taskEnd = Timestamps.toMillis(task.end) / millisThresh;
      long cpuStart = Timestamps.toMillis(cpu.start) / millisThresh;
      long cpuEnd = Timestamps.toMillis(cpu.end) / millisThresh;

      if (cpuEnd < taskStart) {
        if (!cpus.hasNext()) {
          break;
        }
        cpu = cpus.next();
        continue;
      } else if (taskEnd < cpuStart) {
        if (!tasks.hasNext()) {
          break;
        }
        task = tasks.next();
        continue;
      }

      jiffies.add(
          new Difference(
              Timestamps.fromMillis(millisThresh * (taskStart < cpuStart ? taskStart : cpuStart)),
              Timestamps.fromMillis(millisThresh * (taskEnd > cpuEnd ? taskEnd : cpuEnd)),
              virtualizeJiffies(task, cpu)));

      if (!tasks.hasNext() || !cpus.hasNext()) {
        break;
      }

      task = tasks.next();
      cpu = cpus.next();

      break;
    }

    return jiffies;
  }

  /** Sort the samples by timestamp and compute the forward difference between pairs. */
  static List<Difference<CpuReading>> processCpus(List<CpuSample> samples) {
    ArrayList<Difference<CpuReading>> diffs = new ArrayList<>();
    Optional<CpuSample> last = Optional.empty();
    for (CpuSample sample :
        samples
            .stream()
            .sorted((s1, s2) -> Timestamps.compare(s1.getTimestamp(), s2.getTimestamp()))
            .collect(toList())) {
      if (last.isPresent()) {
        Difference<CpuReading> diff =
            new Difference(
                last.get().getTimestamp(), sample.getTimestamp(), difference(last.get(), sample));
        diffs.add(diff);
      }
      last = Optional.of(sample);
    }
    return diffs;
  }

  /** Take the forward difference of the samples' jiffies. */
  private static List<CpuReading> difference(CpuSample first, CpuSample second) {
    Map<Integer, CpuReading> firstMap =
        first
            .getReadingList()
            .stream()
            .collect(toMap(reading -> reading.getCpu(), reading -> reading));
    return second
        .getReadingList()
        .stream()
        // TODO(timur): if this fails, we have a big problem
        .map(reading -> difference(firstMap.get(reading.getCpu()), reading))
        .collect(toList());
  }

  /** Take the forward difference of the readings' jiffies. */
  private static CpuReading difference(CpuReading first, CpuReading second) {
    return CpuReading.newBuilder()
        .setCpu(first.getCpu())
        .setUser(second.getUser() - first.getUser())
        .setNice(second.getNice() - first.getNice())
        .setSystem(second.getSystem() - first.getSystem())
        .setIdle(second.getIdle() - first.getIdle())
        .setIowait(second.getIowait() - first.getIowait())
        .setIrq(second.getIrq() - first.getIrq())
        .setSoftirq(second.getSoftirq() - first.getSoftirq())
        .setSteal(second.getSteal() - first.getSteal())
        .setGuest(second.getGuest() - first.getGuest())
        .setGuestNice(second.getGuestNice() - first.getGuestNice())
        .build();
  }

  /** Sort the samples by timestamp and compute the forward difference between pairs. */
  static List<Difference<TaskReading>> processTasks(List<TaskSample> samples) {
    ArrayList<Difference<TaskReading>> diffs = new ArrayList<>();
    Optional<TaskSample> last = Optional.empty();
    for (TaskSample sample :
        samples
            .stream()
            .sorted((s1, s2) -> Timestamps.compare(s1.getTimestamp(), s2.getTimestamp()))
            .collect(toList())) {
      if (last.isPresent()) {
        Difference<TaskReading> diff =
            new Difference(
                last.get().getTimestamp(), sample.getTimestamp(), difference(last.get(), sample));
        diffs.add(diff);
      }
      last = Optional.of(sample);
    }
    return diffs;
  }

  /** Take the forward difference of the samples' jiffies. */
  private static List<TaskReading> difference(TaskSample first, TaskSample second) {
    Map<Integer, TaskReading> firstMap =
        first.getReadingList().stream().collect(toMap(s -> s.getTaskId(), s -> s));
    return second
        .getReadingList()
        .stream()
        .map(
            reading -> {
              if (firstMap.containsKey(reading.getTaskId())) {
                return Optional.of(difference(firstMap.get(reading.getTaskId()), reading));
              } else {
                return Optional.empty();
              }
            })
        .filter(Optional::isPresent)
        // TODO(timur): why did i have to do this?
        .map(r -> (TaskReading) r.get())
        .collect(toList());
  }

  /** Take the forward difference of the readings' jiffies. */
  private static TaskReading difference(TaskReading first, TaskReading second) {
    return TaskReading.newBuilder()
        .setTaskId(first.getTaskId())
        .setCpu(first.getCpu())
        .setName(first.getName())
        .setUser(second.getUser() - first.getUser())
        .setSystem(second.getSystem() - first.getSystem())
        .build();
  }

  /** Compute the jiffies consumption of tasks with task jiff / cpu jiff / total task jiff. */
  private static List<Activity> virtualizeJiffies(
      Difference<TaskReading> task, Difference<CpuReading> cpu) {
    double[] cpuJiffies = getTotalJiffies(cpu);
    List<Activity> jiffies =
        task.data.stream().map(JiffiesVirtualizer::virtualizeTask).collect(toList());
    double[] taskJiffies = new double[cpuJiffies.length];
    jiffies
        .stream()
        .collect(groupingBy(a -> a.cpu))
        .entrySet()
        .forEach(
            e ->
                taskJiffies[e.getKey()] = e.getValue().stream().mapToDouble(a -> a.activity).sum());
    return jiffies
        .stream()
        .map(
            jiff ->
                new Activity(
                    jiff.id,
                    jiff.cpu,
                    jiff.activity
                        / (Math.max(cpuJiffies[jiff.cpu], 1) * Math.max(taskJiffies[jiff.cpu], 1))))
        .collect(toList());
  }

  private static double[] getTotalJiffies(Difference<CpuReading> cpu) {
    double[] cpuJiffies = new double[cpu.data.size()];
    for (CpuReading reading : cpu.data) {
      cpuJiffies[reading.getCpu()] =
          reading.getUser()
              + reading.getSystem()
              + reading.getNice()
              + reading.getIrq()
              + reading.getSoftirq()
              + reading.getGuest()
              + reading.getGuestNice();
    }
    return cpuJiffies;
  }

  private static Activity virtualizeTask(TaskReading task) {
    return new Activity(
        task.getTaskId(), task.getCpu(), (double) (task.getUser() + task.getSystem()));
  }

  private JiffiesVirtualizer() {}
}
