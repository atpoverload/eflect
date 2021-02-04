package eflect.data;

import java.time.Instant;

/** Sample of consumed global energy. */
// TODO(timur): this will be concrete until we design domains
public final class EnergySample implements Sample {
  private final Instant timestamp;
  private final double[][] stats;

  public EnergySample(Instant timestamp, double[][] stats) {
    this.timestamp = timestamp;
    this.stats = stats;
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Returns the energy broken down by domain and component. */
  // TODO(timur): eventually we have to think if the domains need to be further abstracted
  public double[][] getEnergy() {
    return stats;
  }
}
