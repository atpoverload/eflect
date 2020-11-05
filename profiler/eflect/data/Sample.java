package eflect.data;

import java.time.Instant;

/** Interface for a piece of timestamped data. */
public abstract class Sample {
  private final String[] stats;
  private final Instant timestamp;

  public Sample(String[] stats, Instant timestamp) {
    this.stats = stats;
    this.timestamp = timestamp;
  }

  public final String[] getStats() {
    return stats;
  }

  public final Instant getTimestamp() {
    return timestamp;
  }
}
