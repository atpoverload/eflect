package eflect.virtualization;

final class Activity {
  final int id;
  final int cpu;
  final double activity;

  Activity(int id, int cpu, double activity) {
    this.id = id;
    this.cpu = cpu;
    this.activity = activity;
  }
}
