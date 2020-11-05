package eflect.data;

import static jrapl.Rapl.SOCKET_COUNT;
import static jrapl.Rapl.getEnergyStats;

import java.time.Instant;

/** Snapshot of the machine's energy from rapl. */
public final class RaplSample extends Sample {
  // flattens jRAPL's output into just energy
  // TODO(timurbey): i'd prefer to read the raw, unparsed data
  static RaplSample readEnergy() {
    double[] energy = new double[SOCKET_COUNT];
    double[][] stats = getEnergyStats();
    for (int i = 0; i < SOCKET_COUNT; i++) {
      double stat = 0;
      for (double e : stats[i]) {
        stat += e;
      }
      energy[i] = stat;
    }
    return new RaplSample(energy, Instant.now());
  }

  private final double[] energy;

  RaplSample(double[] energy, Instant timestamp) {
    super(new String[0], timestamp);
    this.energy = energy;
  }

  public double[] getEnergy() {
    return energy;
  }
}
