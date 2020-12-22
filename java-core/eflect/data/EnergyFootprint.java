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
  public final int domain;
  public final double energy;
  public final String[] stackTrace;

  private EnergyFootprint(
      long id,
      String name,
      Instant start,
      Instant end,
      int domain,
      double energy,
      String[] stackTrace) {
    this.id = id;
    this.name = name;
    this.start = start;
    this.end = end;
    this.domain = domain;
    this.energy = energy;
    this.stackTrace = stackTrace;
  }

  @Override
  public String toString() {
    return String.join(System.lineSeparator(),
      name + "(" + id + ")",
      "start: " + start,
      "end: " + end,
      "domain: " + domain,
      "energy: " + energy);
  }

  public static final class Builder {
    private long id = -1;
    private String name = "";
    private Instant start = Instant.EPOCH;
    private Instant end = Instant.EPOCH;
    private int domain = -1;
    private double energy = 0;
    private String[] stackTrace = new String[0];

    public Builder() {}

    public Builder setId(long id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
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

    public Builder setDomain(int domain) {
      this.domain = domain;
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
      return new EnergyFootprint(id, name, start, end, domain, energy, stackTrace);
    }
  }
}
