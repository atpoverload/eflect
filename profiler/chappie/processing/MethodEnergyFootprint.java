package chappie.processing;

import eflect.EnergyFootprint;
import java.time.Instant;
import java.util.Map;

/** Representation of the application's energy usage over a time interval. */
// TODO(timur): this can't be usefully exchanged.
public final class MethodEnergyFootprint extends EnergyFootprint<String> {
  private final Map<String, Double> energy;

  public MethodEnergyFootprint(Instant start, Instant end, Map<String, Double> energy) {
    super(start, end);
    this.energy = energy;
  }

  @Override
  public double getEnergy() {
    double energy = 0;
    for (String method: this.energy.keySet()) {
      energy += this.energy.get(method);
    }
    return energy;
  }

  @Override
  public double getEnergy(String method) {
    return this.energy.get(method);
  }

  @Override
  public String toString() {
    return energy.toString();
  }
}
