// package eflect.virtualization;
//
// import static java.util.stream.Collectors.toList;
// import static java.util.stream.Collectors.toMap;
//
// import com.google.protobuf.Timestamp;
// import com.google.protobuf.util.Timestamps;
// import eflect.sample.CpuReading;
// import eflect.sample.CpuSample;
// import eflect.sample.TaskReading;
// import eflect.sample.TaskSample;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// class Virtualization {
//   public static Map<Long, Difference<JiffiesConsumption>> virtualize(EflectDataSet dataSet) {
//     Map<Long, Difference<CpuReading>> cpus =
//         processCpu(dataSet).stream().collect(toMap(s -> Timestamps.toMillis(s.start), s -> s));
//     Map<Long, Difference<TaskReading>> tasks =
//         processTasks(dataSet).stream().collect(toMap(s -> Timestamps.toMillis(s.start), s -> s));
//
//     HashMap<Long, Difference<JiffiesConsumption>> trace = new HashMap<>();
//     for (long timestamp : tasks.keySet()) {
//       if (!cpus.containsKey(timestamp)) {
//         continue;
//       }
//
//       Difference<CpuReading> cpu = cpus.get(timestamp);
//       int[] cpuJiffies = new int[cpu.readings.size()];
//       for (CpuReading reading : cpu.readings) {
//         cpuJiffies[reading.getCpu()] =
//             reading.getUser()
//                 + reading.getSystem()
//                 + reading.getNice()
//                 + reading.getIrq()
//                 + reading.getSoftirq()
//                 + reading.getGuest()
//                 + reading.getGuestNice();
//         System.out.println(reading);
//       }
//       Difference<TaskReading> task = tasks.get(timestamp);
//       Difference<JiffiesConsumption> jiffies =
//           new Difference<JiffiesConsumption>(task.start, task.end);
//       int totalTaskJiffies = 0;
//       for (int i = 0; i < task.readings.size(); i++) {
//         TaskReading reading = task.readings.get(i);
//         int taskJiffies = reading.getUser() + reading.getSystem();
//         int cpuJiff = cpuJiffies[reading.getCpu()] > 0 ? cpuJiffies[reading.getCpu()] : 1;
//         JiffiesConsumption jiffiesConsumption = new JiffiesConsumption();
//         jiffiesConsumption.taskId = reading.getTaskId();
//         jiffiesConsumption.cpu = reading.getCpu();
//         jiffiesConsumption.consumption = taskJiffies / cpuJiff;
//         totalTaskJiffies += taskJiffies;
//         jiffies.readings.add(jiffiesConsumption);
//       }
//       for (int i = 0; i < jiffies.readings.size(); i++) {
//         JiffiesConsumption jiffiesConsumption = jiffies.readings.get(i);
//         jiffiesConsumption.consumption = jiffiesConsumption.consumption / totalTaskJiffies;
//       }
//       trace.put(Timestamps.toMillis(jiffies.start), jiffies);
//     }
//     return trace;
//   }
//
//   private static ArrayList<Difference<CpuReading>> processCpu(EflectDataSet dataSet) {
//     ArrayList<Difference<CpuReading>> cpu = new ArrayList<>();
//     CpuSample lastSample = CpuSample.getDefaultInstance();
//     List<CpuSample> samples =
//         dataSet
//             .getCpuList()
//             .stream()
//             .sorted((s1, s2) -> Timestamps.compare(s1.getTimestamp(), s2.getTimestamp()))
//             .collect(toList());
//     for (CpuSample sample : samples) {
//       if (!lastSample.equals(CpuSample.getDefaultInstance())) {
//         cpu.add(difference(lastSample, sample));
//       }
//       lastSample = sample;
//     }
//
//     return cpu;
//   }
//
//   private static ArrayList<Difference<TaskReading>> processTasks(EflectDataSet dataSet) {
//     ArrayList<Difference<TaskReading>> tasks = new ArrayList<>();
//     TaskSample lastSample = TaskSample.getDefaultInstance();
//     List<TaskSample> samples =
//         dataSet
//             .getTaskList()
//             .stream()
//             .sorted((s1, s2) -> Timestamps.compare(s1.getTimestamp(), s2.getTimestamp()))
//             .collect(toList());
//     for (TaskSample sample : samples) {
//       if (!lastSample.equals(TaskSample.getDefaultInstance())) {
//         tasks.add(difference(lastSample, sample));
//       }
//       lastSample = sample;
//     }
//
//     return tasks;
//   }
//
//   private static Difference<CpuReading> difference(CpuSample first, CpuSample second) {
//     Difference<CpuReading> diff = new Difference<>(first.getTimestamp(), second.getTimestamp());
//     for (int i = 0; i < first.getReadingCount(); i++) {
//       diff.readings.add(
//           CpuReading.newBuilder()
//               .setCpu(first.getReading(i).getCpu())
//               .setUser(second.getReading(i).getUser() - first.getReading(i).getUser())
//               .setNice(second.getReading(i).getNice() - first.getReading(i).getNice())
//               .setSystem(second.getReading(i).getSystem() - first.getReading(i).getSystem())
//               .setIdle(second.getReading(i).getIdle() - first.getReading(i).getIdle())
//               .setIowait(second.getReading(i).getIowait() - first.getReading(i).getCpu())
//               .setIrq(second.getReading(i).getIrq() - first.getReading(i).getIrq())
//               .setSoftirq(second.getReading(i).getSoftirq() - first.getReading(i).getSoftirq())
//               .setSteal(second.getReading(i).getSteal() - first.getReading(i).getSteal())
//               .setGuest(second.getReading(i).getGuest() - first.getReading(i).getGuest())
//               .setGuestNice(
//                   second.getReading(i).getGuestNice() - first.getReading(i).getGuestNice())
//               .build());
//     }
//     return diff;
//   }
//
//   private static Difference<TaskReading> difference(TaskSample first, TaskSample second) {
//     Difference<TaskReading> diff = new Difference<>(first.getTimestamp(), second.getTimestamp());
//     Map<Integer, TaskReading> r1 =
//         first.getReadingList().stream().collect(toMap(s -> s.getTaskId(), s -> s));
//     Map<Integer, TaskReading> r2 =
//         second.getReadingList().stream().collect(toMap(s -> s.getTaskId(), s -> s));
//     for (int id : r1.keySet()) {
//       if (!r2.containsKey(id)) {
//         continue;
//       }
//       diff.readings.add(
//           TaskReading.newBuilder()
//               .setTaskId(id)
//               .setCpu(r1.get(id).getCpu())
//               .setName(r1.get(id).getName())
//               .setUser(r2.get(id).getUser() - r1.get(id).getUser())
//               .setSystem(r2.get(id).getSystem() - r1.get(id).getSystem())
//               .build());
//     }
//     return diff;
//   }
//
//   private static class Difference<T> {
//     private final Timestamp start;
//     private final Timestamp end;
//     private final ArrayList<T> readings = new ArrayList<>();
//
//     private Difference(Timestamp start, Timestamp end) {
//       this.start = start;
//       this.end = end;
//     }
//   }
//
//   private static class VirtualizedJiffies {
//     private int taskId;
//     private int cpu;
//     private double consumption;
//
//     @Override
//     public String toString() {
//       return String.join(
//           "-", Integer.toString(taskId), Integer.toString(cpu), Double.toString(consumption));
//     }
//   }
// }
