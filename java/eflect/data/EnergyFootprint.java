package eflect.data;

import java.time.Instant;
import java.util.ArrayList;

/** Report for energy consumed by a thread. */
// TODO(timur): this needs to be a proto; it's gotten too bulky
public final class EnergyFootprint {
  public final long id;
  public final String name;
  public final Instant start;
  public final Instant end;
  public final double energy;
  public final String stackTrace;

  private EnergyFootprint(
      long id, String name, Instant start, Instant end, double energy, String stackTrace) {
    this.id = id;
    this.name = name;
    this.start = start;
    this.end = end;
    this.energy = energy;
    this.stackTrace = stackTrace;
  }

  @Override
  public String toString() {
    if (stackTrace == "") {
      return String.join(
          ",",
          Long.toString(id),
          name,
          start.toString(),
          end.toString(),
          Double.toString(energy),
          "");
    }
    String[] traces = stackTrace.split("@");
    String[] footprints = new String[traces.length];
    for (int i = 0; i < traces.length; i++) {
      if (!traces[i].isEmpty()) {
        footprints[i] =
            String.join(
                ",",
                Long.toString(id),
                name,
                start.toString(),
                end.toString(),
                Double.toString(energy / traces.length),
                traces[i]);
      }
    }
    return String.join(System.lineSeparator(), footprints);
  }

  public Builder toBuilder() {
    Builder builder =
        new Builder().setId(id).setName(name).setStart(start).setEnd(end).setEnergy(energy);
    if (!stackTrace.isEmpty()) {
      for (String trace : stackTrace.split("@")) {
        builder.addStackTrace(trace);
      }
    }
    return builder;
  }

  public static final class Builder {
    private long id;
    private String name = "";
    private Instant start = Instant.EPOCH;
    private Instant end = Instant.EPOCH;
    private double energy = 0;
    private ArrayList<String> stackTrace = new ArrayList<>();;

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

    public Builder addStackTrace(String stackTrace) {
      this.stackTrace.add(stackTrace);
      return this;
    }

    public EnergyFootprint build() {
      if (stackTrace.isEmpty()) {
        return new EnergyFootprint(id, name, start, end, energy, "");
      } else {
        return new EnergyFootprint(id, name, start, end, energy, String.join("@", stackTrace));
      }
    }
  }
}
