package eflect.benchmarks;

import static org.openjdk.jmh.results.AggregationPolicy.AVG;
import static org.openjdk.jmh.results.Defaults.PREFIX;
import static org.openjdk.jmh.results.ResultRole.SECONDARY;

import java.util.ArrayList;
import java.util.Collection;
import org.openjdk.jmh.results.Aggregator;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.util.ListStatistics;

/** A result that reports the number of collected vs expected samples from a trial. */
class EflectResult extends Result<EflectResult> {
  private final double[] energies;

  public EflectResult(double[] energies) {
    super(SECONDARY, PREFIX + "eflect", new ListStatistics(energies), "J", AVG);
    this.energies = energies;
  }

  @Override
  protected Aggregator<EflectResult> getThreadAggregator() {
    return new EflectResultAggregator();
  }

  @Override
  protected Aggregator<EflectResult> getIterationAggregator() {
    return new EflectResultAggregator();
  }

  private class EflectResultAggregator implements Aggregator<EflectResult> {
    @Override
    public EflectResult aggregate(Collection<EflectResult> results) {
      ArrayList<Double> energiesList = new ArrayList<Double>();
      for (EflectResult r : results) {
        for (double energy : r.energies) {
          energiesList.add(energy);
        }
      }
      double[] energies = new double[energiesList.size()];
      for (int i = 0; i < energiesList.size(); i++) {
        energies[i] = energiesList.get(i);
      }
      return new EflectResult(energies);
    }
  }
}
