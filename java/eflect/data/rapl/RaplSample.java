package eflect.data.rapl;

import eflect.data.EnergySample;
import java.time.Instant;

/** A sample of energy consumed as reported by jRAPL. */
public final class RaplSample implements EnergySample {
  private final Instant timestamp;
  private final double[][] stats;

  public RaplSample(Instant timestamp, double[][] stats) {
    this.timestamp = timestamp;
    this.stats = stats;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public double[][] getEnergy() {
    return stats;
  }
}
