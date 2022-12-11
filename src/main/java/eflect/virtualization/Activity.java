package eflect.virtualization;

final class Activity {
  public final int id;
  public final int cpu;
  public final double activity;

  public Activity(int id, int cpu, double activity) {
    this.id = id;
    this.cpu = cpu;
    this.activity = activity;
  }
}
