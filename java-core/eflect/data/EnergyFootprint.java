package eflect.data;

import java.time.Instant;

/** Report for energy consumed by a thread. */
public final class EnergyFootprint {
  public static Builder newBuilder() {
    return new Builder();
  }

  public final long id;
  public final String name;
  public final Instant start;
  public final Instant end;
  public final double energy;
  public final String[] stackTrace;

  private EnergyFootprint(
      long id, String name, Instant start, Instant end, double energy, String[] stackTrace) {
    this.id = id;
    this.name = name;
    this.start = start;
    this.end = end;
    this.energy = energy;
    this.stackTrace = stackTrace;
  }

  @Override
  public String toString() {
    return String.join(
        ",",
        Long.toString(id),
        name,
        start.toString(),
        end.toString(),
        Double.toString(energy),
        String.join("#", stackTrace));
  }

  public static final class Builder {
    private long id;
    private String name = "";
    private Instant start = Instant.EPOCH;
    private Instant end = Instant.EPOCH;
    private double energy = 0;
    private String[] stackTrace = new String[0];

    public Builder() {}

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setId(long id) {
      this.id = id;
      return this;
    }

    public Builder setStart(Instant start) {
      this.start = start;
      return this;
    }

    public Builder setEnd(Instant end) {
      this.end = end;
      return this;
    }

    public Builder setEnergy(double energy) {
      this.energy = energy;
      return this;
    }

    public Builder setStackTrace(String[] stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }

    public EnergyFootprint build() {
      return new EnergyFootprint(id, name, start, end, energy, stackTrace);
    }
  }
}
