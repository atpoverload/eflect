package eflect;

import java.time.Duration;
import java.time.Instant;

/** Representation of the application's energy usage over some time interval. */
public final class EnergyFootprint {
  private final Instant start;
  private final Instant end;
  private final Duration duration;
  private final double[] energy;

  EnergyFootprint(Instant start, Instant end, double[] energy) {
    this.start = start;
    this.end = end;
    this.duration = Duration.between(start, end);

    this.energy = energy;
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public double getEnergy() {
    double energy = 0;
    for (int i = 0; i < this.energy.length; i++) {
      energy += this.energy[i];
    }
    return energy;
  }

  public double getEnergy(int socket) {
    return this.energy[socket];
  }

  public double getPower() {
    return getEnergy() / duration.toMillis() / 1000;
  }

  public double getPower(int socket) {
    return getEnergy(socket) / duration.toMillis() / 1000;
  }
}
