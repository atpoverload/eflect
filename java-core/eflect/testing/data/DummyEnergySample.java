package eflect.testing.data;

import eflect.data.EnergySample;
import java.time.Instant;

/** A sample of energy consumed as reported by jRAPL. */
public final class DummyEnergySample implements EnergySample {
  private final Instant timestamp;
  private final double[][] energy;

  public DummyEnergySample(Instant timestamp, double energy) {
    this.timestamp = timestamp;
    this.energy = new double[][] {new double[] {energy}};
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public double[][] getEnergy() {
    return energy;
  }
}
