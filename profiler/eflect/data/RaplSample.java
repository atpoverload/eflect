package eflect.data;

import static eflect.utils.OsUtils.readEnergy;

import java.time.Instant;
import java.util.Arrays;

/** Snapshot of the machine's rapl counters. */
public final class RaplSample implements Sample {
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
