package eflect;

import java.time.Duration;
import java.time.Instant;

// TODO(timurbey): find an exchangable data format
/** Abstract representation of the application's energy consumption. */
public abstract class EnergyFootprint<D> {
  private final Instant start;
  private final Instant end;
  private final Duration duration;

  public EnergyFootprint(Instant start, Instant end) {
    this.start = start;
    this.end = end;
    this.duration = Duration.between(start, end);
  }

  public abstract double getEnergy();

  public abstract double getEnergy(D domain);

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public double getPower() {
    return getEnergy() / duration.toMillis() / 1000;
  }

  public double getPower(D domain) {
    return getEnergy(domain) / duration.toMillis() / 1000;
  }
}
