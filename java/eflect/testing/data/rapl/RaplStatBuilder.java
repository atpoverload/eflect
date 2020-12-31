package eflect.testing.data.jiffies;

/** A class that builds a double[] for jrapl. */
public final class RaplStatBuilder {
  private final double[][] energy;

  public RaplStatBuilder(double energy) {
    this.energy = new double[][] {new double[] {energy}};
  }

  public double[][] build() {
    return energy;
  }
}
