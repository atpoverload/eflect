package eflect.data;

import static jrapl.Rapl.getEnergyStats;
import static jrapl.Rapl.SOCKET_COUNT;

import java.time.Instant;
import java.util.Arrays;

/** Snapshot of the machine's rapl counters. */
public final class RaplSample implements Sample {
  // flattens jRAPL's output into just energy
  private static double[] readEnergy() {
    double[] energy = new double[SOCKET_COUNT];
    double[][] stats = getEnergyStats();
    for (int i = 0; i < SOCKET_COUNT; i++) {
      double stat = 0;
      for (double e: stats[i]) {
        stat += e;
      }
      energy[i] = stat;
    }
    return energy;
  }

  private final double[] energy;
  private final Instant timestamp;

  public RaplSample() {
    this.energy = readEnergy();
    this.timestamp = Instant.now();
  }

  public double[] getEnergy() {
    return energy;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
