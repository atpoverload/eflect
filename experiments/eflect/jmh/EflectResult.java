package eflect.jmh;

import static org.openjdk.jmh.results.AggregationPolicy.AVG;
import static org.openjdk.jmh.results.Defaults.PREFIX;
import static org.openjdk.jmh.results.ResultRole.SECONDARY;

import eflect.data.EnergyFootprint;
import java.util.ArrayList;
import java.util.Collection;
import org.openjdk.jmh.results.Aggregator;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.util.ListStatistics;

/** A result that reports the number of collected vs expected samples from a trial. */
class EflectResult extends Result<EflectResult> {
  private final long ops;
  private final Collection<EnergyFootprint> footprints;

  public EflectResult(Collection<EnergyFootprint> footprints, long ops) {
    super(
        SECONDARY,
        PREFIX + "eflect",
        new ListStatistics(footprints.stream().map(x -> x.energy).toArray(double[]::new)),
        "J",
        AVG);
    this.ops = ops;
    this.footprints = footprints;
  }

  @Override
  protected Aggregator<EflectResult> getThreadAggregator() {
    return new EflectResultAggregator();
  }

  @Override
  protected Aggregator<EflectResult> getIterationAggregator() {
    return new EflectResultAggregator();
  }

  @Override
  public String extendedInfo() {
    return getStatistics().getSum();
  }

  private class EflectResultAggregator implements Aggregator<EflectResult> {
    @Override
    public EflectResult aggregate(Collection<EflectResult> results) {
      long ops = 0;
      ArrayList<EnergyFootprint> footprints = new ArrayList<>();
      for (EflectResult result : results) {
        footprints.addAll(result.footprints);
        ops += result.ops;
      }
      return new EflectResult(footprints, ops);
    }
  }
}
