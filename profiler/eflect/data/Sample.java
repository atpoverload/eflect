package eflect.data;

import java.time.Instant;

/** Interface for a piece of timestamped data. */
public interface Sample {
  String[] getStats();

  default Instant getTimestamp() {
    return Instant.EPOCH;
  }
}
