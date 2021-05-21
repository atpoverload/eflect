package eflect.data;

import java.time.Instant;
import java.util.ArrayList;

/** Sample of consumed global energy. */
// TODO: this will be concrete until we design domains
public final class EnergySample implements Sample {
  // TODO: still too crude
  public static class Component {
    public static int CPU = 1;
    public static int DRAM = 0;
    public static int PACKAGE = 2;
    public static int GPU = 3;
  }

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
  // TODO: eventually we have to think if the domains need to be further abstracted
  public double[][] getEnergy() {
    return stats;
  }

  @Override
  public String toString() {
    ArrayList<String> stats = new ArrayList<>();
    double[][] energy = getEnergy();
    for (int domain = 0; domain < energy.length; domain++) {
      String[] domainEnergy = new String[energy[domain].length];
      for (int component = 0; component < energy[domain].length; component++) {
        domainEnergy[component] = Double.toString(energy[domain][component]);
      }
      stats.add(
          String.join(
              ",", timestamp.toString(), Integer.toString(domain), String.join(",", domainEnergy)));
    }
    return String.join(System.lineSeparator(), stats);
  }
}
