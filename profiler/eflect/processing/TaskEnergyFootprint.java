package eflect.processing;

import eflect.EnergyFootprint;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

// TODO(timurbey): where should you live?
/** Representation of the application's energy usage by executing task. */
public final class TaskEnergyFootprint extends EnergyFootprint<Long> {
  private final HashMap<Long, Double> energy = new HashMap<>();

  TaskEnergyFootprint(Instant start, Instant end, Map<Long, Double> energy) {
    super(start, end);
    for (long task: energy.keySet()) {
      this.energy.put(task, energy.get(task));
    }
  }

  @Override
  public double getEnergy() {
    double energy = 0;
    for (long tid: this.energy.keySet()) {
      energy += this.energy.get(tid);
    }
    return energy;
  }

  @Override
  public double getEnergy(Long tid) {
    return this.energy.get(tid);
  }

  @Override
  public String toString() {
    return energy.toString();
  }
}
