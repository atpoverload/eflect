package eflect;

import clerk.FixedPeriodClerk;
import eflect.data.AccountantMerger;
import eflect.data.EnergyAccountant;
import eflect.data.EnergyFootprint;
import eflect.data.StackTraceAligner;
import eflect.data.jiffies.JiffiesAccountant;
import java.time.Duration;
import java.util.Collection;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/** A profiler that estimates the energy consumed by the current application. */
public abstract class Eflect extends FixedPeriodClerk<Collection<EnergyFootprint>> {
  protected Eflect(
      Collection<Supplier<?>> sources,
      int domainCount,
      double wrapAroundEnergy,
      IntUnaryOperator domainConversion,
      Duration period) {
    super(
        sources,
        new StackTraceAligner(
            new AccountantMerger<EnergyFootprint>(
                () ->
                    new EnergyAccountant(
                        domainCount,
                        wrapAroundEnergy,
                        new JiffiesAccountant(domainCount, domainConversion)))),
        period);
  }
}
