package eflect.data.jiffies;

/** A class that contains relevant information from /proc/[pid]/task/[tid]/stat. */
final class TaskStat {
  final long id;
  final String name;
  final int cpu;
  final long userJiffies;
  final long systemJiffies;

  TaskStat(long id, String name, int cpu, long userJiffies, long systemJiffies) {
    this.id = id;
    this.name = name;
    this.cpu = cpu;
    this.jiffies = jiffies;
  }

  @Override
  public String toString() {
    return String.join(
        ",",
        Long.toString(id),
        name,
        Integer.toString(cpu),
        Long.toString(userJiffies),
        Long.toString(systemJiffies));
  }
}
